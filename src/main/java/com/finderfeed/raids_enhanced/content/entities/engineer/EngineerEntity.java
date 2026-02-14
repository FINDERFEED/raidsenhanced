package com.finderfeed.raids_enhanced.content.entities.engineer;

import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.init.FDEDataSerializers;
import com.finderfeed.fdlib.nbt.AutoSerializable;
import com.finderfeed.fdlib.nbt.SerializableField;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.TransitionAnimation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.HeadControllerContainer;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.IHasHead;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.fdlib.util.client.particles.BoneAttachedParticles;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.REUtil;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.content.entities.ball_lightning.BallLightningEntity;
import com.finderfeed.raids_enhanced.content.entities.vertical_lightning_strike.VerticalLightningStrikeAttack;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.slash_particle.SlashParticleOptions;
import com.finderfeed.raids_enhanced.content.util.HorizontalCircleRandomDirections;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class EngineerEntity extends FDRaider implements AutoSerializable, IHasHead<EngineerEntity> {


    public static final String MAIN_LAYER = "IDLE";
    public static final String WALKING_LAYER = "WALKING";

    public static final String LIGHTNING_START = "lightning_start";

    public static final EntityDataAccessor<Byte> BYTE_PARTICLE_TRIGGER = SynchedEntityData.defineId(EngineerEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> LASER_ACTIVE = SynchedEntityData.defineId(EngineerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vec3> LASER_TARGET = SynchedEntityData.defineId(EngineerEntity.class, FDEDataSerializers.VEC3.get());

    private boolean rotatingFromBodyRot = true;

    @SerializableField
    private int lightningRayAttackCooldown = 100;

    @SerializableField
    private int lightningsCooldown = 100;

    private Vec3 laserTargetOld;

    private static FDModel clientModel;
    private static FDModel serverModel;

    private BoneAttachedParticles boneAttachedParticles;

    public boolean isUsingElectricRay = false;
    public boolean isUsingLightningsAttack = false;

    private HeadControllerContainer<EngineerEntity> headControllerContainer;

    public EngineerEntity(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
        this.getNavigation().setCanFloat(true);

        this.lookControl = headControllerContainer = (new HeadControllerContainer<>(this) {
            @Override
            protected boolean resetXRotOnTick() {
                return false;
            }
        }).addHeadController(getModel(level()), "head");

        if (level().isClientSide){
            this.boneAttachedParticles = new BoneAttachedParticles(getModel(level()), this, LIGHTNING_START, Vec3.ZERO);
        }
        this.getModelSystem().getAnimationSystem().setAnimationsApplyListener(this::onAnimationsApplied);
        this.moveControl = new ElectromancerMoveControl(this);
    }

    public static FDModel getModel(Level level){
        if (!level.isClientSide){
            if (serverModel == null){
                serverModel = new FDModel(REModels.ELECTROMANCER.get());
            }
            return serverModel;
        }else{
            if (clientModel == null){
                clientModel = new FDModel(REModels.ELECTROMANCER.get());
            }
            return clientModel;
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));


        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new BallLightningRangedAttack(this, 20));
        this.goalSelector.addGoal(4, new LaserAttackGoal(this));
        this.goalSelector.addGoal(4, new LightningsAttack(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1){
            @Override
            public boolean canUse() {
                return super.canUse() && EngineerEntity.this.getTarget() == null;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && EngineerEntity.this.getTarget() == null;
            }
        });
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide){
            this.lightningRayAttackCooldown = Mth.clamp(lightningRayAttackCooldown - 1,0, Integer.MAX_VALUE);
            this.lightningsCooldown = Mth.clamp(lightningsCooldown - 1,0, Integer.MAX_VALUE);
//            this.lightningsCooldown = 0;
            this.controlIdleAndWalking();
            if (rotatingFromBodyRot) {
                this.setYRot(this.yBodyRot);
            }
        }else{

            this.getHeadControllerContainer().clientTick();
            this.boneAttachedParticles.clientTick(this.position());
        }
    }

    private void onAnimationsApplied(FDModel model, Float pticks) {
        this.getAnimationSystem().setVariable("variable.lightning_hand_offset", this.getXRot());
    }

    private void controlIdleAndWalking(){
        Vec3 movement = this.getDeltaMovement();
        float speed = (float) movement.multiply(1,0,1).length();

        var animSystem = this.getAnimationSystem();
        var ticker = animSystem.getTicker(MAIN_LAYER);
        if (ticker != null) {
            var animation = ticker.getAnimation();
            if (speed > 0.01) {
                if (this.getTarget() == null) {
                    this.getLookControl().setLookAt(this.getEyePosition().add(this.getLookAngle()));
                }

                if (this.isIdleAnim(animation) || animation.isToNullTransition()) {
                    this.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_WALK)
                                    .setSpeed(1.5f)
                            .build());
                }

            } else {
                this.getAnimationSystem().stopAnimation(WALKING_LAYER);
                if (this.isWalkingAnim(animation) || animation.isToNullTransition()) {
                    animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_IDLE)
                            .build());
                }
            }
        }else{
            animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_IDLE)
                    .build());
        }
    }

    public boolean isIdleAnim(Animation animation){
        if (animation == null){
            return false;
        }else if (animation instanceof TransitionAnimation transitionAnimation){
            return transitionAnimation.getTransitionTo() == REAnimations.ELECTROMANCER_IDLE.get();
        }else{
            return animation == REAnimations.ELECTROMANCER_IDLE.get();
        }
    }

    public boolean isWalkingAnim(Animation animation){
        if (animation == null){
            return false;
        }else if (animation instanceof TransitionAnimation transitionAnimation){
            return transitionAnimation.getTransitionTo() == REAnimations.ELECTROMANCER_WALK.get();
        }else{
            return animation == REAnimations.ELECTROMANCER_WALK.get();
        }
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {

        if (!level().isClientSide && this.isLaserActive()){

            if (src.getEntity() instanceof LivingEntity livingEntity && livingEntity != this && livingEntity.distanceTo(this) < 5){
                var damage = this.getAttributeValue(Attributes.ATTACK_DAMAGE);

                livingEntity.hurt(this.level().damageSources().mobAttack(this), (float) (damage * 0.5f));
                Vec3 between = livingEntity.position().subtract(this.position());
                Vec3 pushVector = between.normalize().scale(2f);

                if (livingEntity.onGround()){
                    pushVector = pushVector.add(0,0.25,0);
                }
                if (livingEntity instanceof ServerPlayer serverPlayer){
                    FDLibCalls.setServerPlayerSpeed(serverPlayer, pushVector);
                    serverPlayer.hasImpulse = true;
                }else{
                    livingEntity.setDeltaMovement(pushVector);
                    livingEntity.hasImpulse = true;
                }
            }

            return false;
        }

        return super.hurt(src, amount);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor == LASER_TARGET){
            this.laserTargetOld = this.getLaserTarget(1);
        }else if (accessor == BYTE_PARTICLE_TRIGGER){
            if (level().isClientSide) {
                this.boneAttachedParticles.addParticle(new SimpleTexturedParticleOptions(REParticles.LIGHTNING_EXPLOSION.get(), 1f, 8), this.position());
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.autoLoad(tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.autoSave(tag);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LASER_TARGET, Vec3.ZERO);
        builder.define(BYTE_PARTICLE_TRIGGER, (byte)0);
        builder.define(LASER_ACTIVE, false);
    }

    public void setLaserState(boolean state){
        this.getEntityData().set(LASER_ACTIVE, state);
    }

    public void setLaserTarget(Vec3 pos){
        this.getEntityData().set(LASER_TARGET, pos);
    }

    public Vec3 getLaserTarget(float pticks){
        var current = this.getEntityData().get(LASER_TARGET);
        if (laserTargetOld == null){
            laserTargetOld = current;
        }
        return FDMathUtil.interpolateVectors(laserTargetOld, current, pticks);
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    public boolean isLaserActive() {
        return this.getEntityData().get(LASER_ACTIVE);
    }

    private void triggerPrepareParticle(){
        var entityData = this.getEntityData();
        byte b = entityData.get(BYTE_PARTICLE_TRIGGER);
        if (b == 0){
            entityData.set(BYTE_PARTICLE_TRIGGER, (byte)1);
        }else{
            entityData.set(BYTE_PARTICLE_TRIGGER, (byte)0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33306_) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    public HeadControllerContainer<EngineerEntity> getHeadControllerContainer() {
        return headControllerContainer;
    }


    public static class BallLightningRangedAttack extends Goal {

        private EngineerEntity entity;

        private int attackTick = 0;
        private float attackRange;
        private boolean animType;

        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        public BallLightningRangedAttack(EngineerEntity electromancerEntity, float attackRange){
            this.entity = electromancerEntity;
            this.attackRange = attackRange;
        }

        @Override
        public void start() {
            super.start();
            attackTick = 0;
        }

        @Override
        public void stop() {
            super.stop();
            attackTick = 0;
//            this.entity.walkingWithHands = false;
            this.entity.getAnimationSystem().stopAnimation(WALKING_LAYER);
            this.entity.setSpeed(0);
            this.entity.setZza(0);
            this.entity.setXxa(0);
        }

        @Override
        public void tick() {
            super.tick();
            var target = this.entity.getTarget();
            if (target == null) return;
            if (this.entity.distanceTo(target) <= attackRange) {
                this.tickAttack(target);
            }else{
                this.attackTick = 0;
//                this.entity.walkingWithHands = false;
                this.entity.getNavigation().moveTo(target, 1f);
            }
        }

        private void tickAttack(LivingEntity target){

            this.entity.getNavigation().stop();
            this.entity.getLookControl().setLookAt(target);
            this.entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
            this.processStrafing(target);

//            this.entity.walkingWithHands = true;
            this.entity.getAnimationSystem().startAnimation(WALKING_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_WALK_NO_HANDS)
                    .setToNullTransitionTime(10)
                    .build());


            if (attackTick == 0){

                animType = entity.random.nextBoolean();
                this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(animType ? REAnimations.ELECTROMANCER_ATTACK_1 : REAnimations.ELECTROMANCER_ATTACK_2)
                        .setSpeed(0.9f)
                        .important()
                        .build());
            }else if (attackTick == 5){

                this.entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), RESounds.ENGINEER_BALL_LIGHTNING_LAUNCH.get(), SoundSource.HOSTILE, 2f, entity.random.nextFloat() * 0.1f + 0.9f);

                Vec3 pos = this.entity.position();

                Vec3 forward = this.entity.getForward().multiply(1,0,1).normalize();
                Vec3 left = forward.yRot(FDMathUtil.FPI / 2);

                Vec3 particlePos;
                Vec3 particleDirection = forward.add(0,-0.1,0);
                float particleTilt;
                if (this.animType){
                    particlePos = pos
                            .add(left.scale(-0.5f))
                            .add(forward.scale(0.5f))
                            .add(0, this.entity.getBbHeight() / 1.5,0);
                    particleTilt = FDMathUtil.FPI / 12f;
                }else{
                    particlePos = pos
                            .add(left.scale(0.1f))
                            .add(forward.scale(0.75f))
                            .add(0, this.entity.getBbHeight() / 1.5,0);
                    particleTilt = -FDMathUtil.FPI / 12f;
                }
                ServerLevel serverLevel = (ServerLevel) this.entity.level();
                SlashParticleOptions options = new SlashParticleOptions(REParticles.ELECTRIC_SLASH.get(), particleDirection, 3, particleTilt, 3, this.animType);
                for (var player : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, this.entity.level(), this.entity.position(), 60f)){
                    serverLevel.sendParticles(player, options, true, particlePos.x, particlePos.y, particlePos.z, 1, 0,0,0,0);

                }

            }else if (attackTick == 8){
                Vec3 targetPos = target.position().add(0,target.getEyeHeight() * 0.9f, 0);
                Vec3 summonPos = this.entity.position().add(0,this.entity.getBbHeight() * 0.6f,0).add(this.entity.getLookAngle().scale(0.25));
                Vec3 between = targetPos.subtract(summonPos);

                float difficultyMultiplier = (float)(14 - this.entity.level().getDifficulty().getId() * 4);
                Vec3 speed = between.normalize()
                        .add(
                                this.entity.random.triangle(0.0, 0.0172275 * difficultyMultiplier / 2),
                                this.entity.random.triangle(0.0, 0.0172275 * difficultyMultiplier / 2),
                                this.entity.random.triangle(0.0, 0.0172275 * difficultyMultiplier / 2)
                        )
                        .scale(2f);
                BallLightningEntity.summon(this.entity, this.entity.level(), summonPos, speed);
            }else if (attackTick > 25){
                attackTick = -1;
            }

            attackTick++;
        }


        private void processStrafing(LivingEntity target){
            double distance = this.entity.distanceTo(target);

            this.strafingTime++;


            if (this.strafingTime >= 20) {
                if ((double)this.entity.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.entity.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (distance > (double)(this.attackRange * 0.75F)) {
                    this.strafingBackwards = false;
                } else if (distance < (double)(this.attackRange * 0.25F)) {
                    this.strafingBackwards = true;
                }

                this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                if (this.entity.getControlledVehicle() instanceof Mob mob) {
                    mob.lookAt(target, 30.0F, 30.0F);
                }

                this.entity.lookAt(target, 30.0F, 30.0F);
            } else {
                this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return !this.entity.isUsingLightningsAttack && !this.entity.isUsingElectricRay && this.entity.getTarget() != null && this.entity.getSensing().hasLineOfSight(this.entity.getTarget());
        }
        @Override
        public boolean canContinueToUse() {
            return this.entity.getTarget() != null && !this.entity.isUsingElectricRay && !this.entity.isUsingLightningsAttack;
        }

    }

    public void spawnLightningAttacksAround(float distance, int lightningsCount, float angle){
        for (var dir : new HorizontalCircleRandomDirections(random, lightningsCount, 0f)){
            Vec3 realDir = dir.yRot(angle).scale(distance);
            BlockPos spawnPosCandidate = BlockPos.containing(this.position().add(realDir));
            this.trySpawnLightning(spawnPosCandidate);
        }
    }

    private void trySpawnLightning(BlockPos candidate){
        if (this.isPosValidForLightning(candidate)){
            this.spawnLighting(candidate);
        }else{
            for (int i = 1; i < 10; i++){
                if (this.isPosValidForLightning(candidate.below(i))){
                    this.spawnLighting(candidate.below(i));
                    return;
                }
            }

            for (int i = 1; i < 10; i++){
                if (this.isPosValidForLightning(candidate.above(i))){
                    this.spawnLighting(candidate.above(i));
                    return;
                }
            }
        }
    }

    private void spawnLighting(BlockPos blockPos){
        Vec3 pos = blockPos.getCenter();
        VerticalLightningStrikeAttack.summon(this, new Vec3(pos.x, Math.floor(pos.y), pos.z));
    }

    private boolean isPosValidForLightning(BlockPos blockPos){
        BlockState state = level().getBlockState(blockPos);
        BlockState stateBelow = level().getBlockState(blockPos.below());
        return state.getCollisionShape(level(), blockPos).isEmpty() && !stateBelow.getCollisionShape(level(), blockPos.below()).isEmpty();
    }

    public static class LightningsAttack extends Goal {


        private EngineerEntity entity;

        private int useTick = 0;

        public LightningsAttack(EngineerEntity electromancerEntity){
            this.entity = electromancerEntity;
        }

        @Override
        public void tick() {

            this.entity.isUsingLightningsAttack= true;

            var target = entity.getTarget();
            if (target == null) return;
            this.entity.getNavigation().stop();

            if (this.entity.getMoveControl() instanceof ElectromancerMoveControl control){
                control.cancelMovement();
            }

            this.entity.rotatingFromBodyRot = false;

            if (useTick < 10){
                this.entity.getLookControl().setLookAt(target);
                this.entity.lookAt(EntityAnchorArgument.Anchor.FEET, target.position());
                this.entity.yBodyRot = this.entity.getYRot();
                this.entity.setSpeed(0);
                this.entity.setZza(0);
                this.entity.setXxa(0);
            }

            int firstLightningStart = 20;

            float ryRot = (float) Math.toRadians(-this.entity.getYRot() + 180);
            if (useTick == 0){
                this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_LIGHTNINGS_CAST)
                                .important()
                        .build());
            }else if (useTick == 13){
                this.entity.triggerPrepareParticle();
            }else if (useTick == firstLightningStart){

                ((ServerLevel)entity.level()).playSound(null, entity.getX(), entity.getY(), entity.getZ(), RESounds.LIGHTNING_STRIKE.get(), SoundSource.HOSTILE, 2f,entity.random.nextFloat() * 0.2f + 0.8f);
                this.castParticles();
                this.damageAndPushAwayEntities();
                this.entity.spawnLightningAttacksAround(4, 4, ryRot);

                this.entity.trySpawnLightning(target.getOnPos());

            }else if (useTick == firstLightningStart + 5){
                this.entity.spawnLightningAttacksAround(8, 8, ryRot + FDMathUtil.FPI / 2);
            }else if (useTick == firstLightningStart + 10){
                this.entity.spawnLightningAttacksAround(12, 12, ryRot);
            }else if (useTick == firstLightningStart + 15){
                this.entity.spawnLightningAttacksAround(16, 16, ryRot + FDMathUtil.FPI / 2);
            }

            useTick++;

        }

        private void damageAndPushAwayEntities(){
            var damage = this.entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
            Vec3 cylinderStart = this.entity.position().add(0,-2,0);
            for (var entity : FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, this.entity.level(), cylinderStart, 3 + this.entity.getBbHeight(),2.5f, e -> e != this.entity)){
                entity.hurt(this.entity.level().damageSources().mobAttack(this.entity), (float) (damage * 1.5f));
                Vec3 between = entity.position().subtract(this.entity.position());
                Vec3 pushVector = between.normalize().scale(2f);
                if (entity.onGround()){
                    pushVector = pushVector.add(0,0.25,0);
                }
                if (entity instanceof ServerPlayer serverPlayer){
                    FDLibCalls.setServerPlayerSpeed(serverPlayer, pushVector);
                    serverPlayer.hasImpulse = true;
                }else{
                    entity.setDeltaMovement(pushVector);
                    entity.hasImpulse = true;
                }
            }
        }

        private void castParticles(){
            Vec3 lpos = this.entity.position().add(this.entity.getForward().multiply(1,0,1).normalize().scale(0.75f));
            REUtil.lightningDebris((ServerLevel) entity.level(), lpos, 60);

            var players = FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, this.entity.level(), this.entity.position(), 40);

            for (var dir : new HorizontalCircleRandomDirections(this.entity.level().random, 6, 0)) {
                Vec3 direction = dir.add(0, 0.5, 0);
                Vec3 pos = lpos.add(direction.multiply(0.4, 0.25, 0.4));
                for (var serverPlayer : players) {

                    ((ServerLevel)this.entity.level()).sendParticles(serverPlayer,
                            new LightningStrikeParticleOptions(REParticles.LIGHTNING_STRIKE.get(), direction, 1f, 4),
                            true, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                }
            }

            for (var serverPlayer : players) {

                ((ServerLevel)this.entity.level()).sendParticles(serverPlayer,
                        new SimpleTexturedParticleOptions(REParticles.VERTICAL_LIGHTNING.get(), 2f, 5),
                        true, lpos.x, lpos.y + 2, lpos.z, 1, 0, 0, 0, 0);
            }
        }

        @Override
        public void start() {
            super.start();
            useTick = 0;
            this.entity.getNavigation().stop();
            this.entity.isUsingLightningsAttack = true;
            this.entity.lightningRayAttackCooldown = 100;
            this.entity.rotatingFromBodyRot = false;
        }

        @Override
        public void stop() {
            super.stop();
            this.entity.isUsingLightningsAttack = false;
            this.entity.lightningRayAttackCooldown = 100;
            this.entity.lightningsCooldown = 150;
            this.entity.rotatingFromBodyRot = true;
            if (this.entity.getTarget() != null){
                this.entity.getLookControl().setLookAt(this.entity.getTarget());
                this.entity.lookAt(EntityAnchorArgument.Anchor.FEET, this.entity.getTarget().position());
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return !this.entity.isUsingElectricRay
                    && this.entity.onGround()
                    && this.entity.random.nextFloat() < 0.05
                    && this.entity.lightningsCooldown == 0
                    && this.entity.getTarget() != null
                    && this.entity.distanceTo(this.entity.getTarget()) < 16
                    && this.entity.getSensing().hasLineOfSight(this.entity.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return this.entity.getTarget() != null && useTick < 50;
        }
    }

    public static class LaserAttackGoal extends Goal {

        private EngineerEntity entity;

        private int useTick = 0;

        public LaserAttackGoal(EngineerEntity electromancerEntity){
            this.entity = electromancerEntity;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            super.start();
            useTick = 0;
            this.entity.getNavigation().stop();
            this.entity.isUsingElectricRay = true;
            this.entity.lightningsCooldown = 100;
        }

        @Override
        public void stop() {
            super.stop();
            this.entity.setLaserState(false);
            this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_RAY_CAST_STOP)
                    .build());
            this.entity.isUsingElectricRay = false;
            this.entity.lightningRayAttackCooldown = 150;
            this.entity.lightningsCooldown = 100;
            this.entity.rotatingFromBodyRot = true;
            if (this.entity.getTarget() != null){
                this.entity.getLookControl().setLookAt(this.entity.getTarget());
                this.entity.lookAt(EntityAnchorArgument.Anchor.FEET, this.entity.getTarget().position());
            }
        }

        @Override
        public void tick() {
            super.tick();

            this.entity.isUsingElectricRay = true;

            var target = entity.getTarget();
            if (target == null) return;
            this.entity.getNavigation().stop();

            if (this.entity.getMoveControl() instanceof ElectromancerMoveControl control){
                control.cancelMovement();
            }

            int stickChargeDuration = 20;

            int rayStartTime = 8;
            int rayDuration = 100;

            boolean laserState = false;

            this.entity.rotatingFromBodyRot = false;

            if (useTick < rayStartTime + stickChargeDuration - 5){
                this.entity.getLookControl().setLookAt(target);
                this.entity.lookAt(EntityAnchorArgument.Anchor.FEET, target.position());
                this.entity.yBodyRot = this.entity.getYRot();
                this.entity.setSpeed(0);
                this.entity.setZza(0);
                this.entity.setXxa(0);
            }



            if (useTick == 0){
                this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_CHARGE_STICK)
                        .build());
            } else if (useTick == 10){

                LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, this.entity.level());
                var t = this.entity.getModelPartPosition(this.entity, LIGHTNING_START, getModel(this.entity.level()));
                Vec3 lpos = new Vec3(t).add(this.entity.position());
                lightningBolt.setPos(lpos);
                lightningBolt.setVisualOnly(true);
                PositionedScreenShakePacket.send((ServerLevel) entity.level(), FDShakeData.builder()
                        .frequency(10f)
                        .amplitude(10f)
                        .inTime(0)
                        .stayTime(0)
                        .outTime(6)
                        .build(),lpos,30);
                this.entity.level().addFreshEntity(lightningBolt);

                this.entity.level().playSound(null, this.entity.getX(), this.entity.getY(), this.entity.getZ(), RESounds.ENGINEER_START_RAY.get(), SoundSource.HOSTILE, 2f, 1f);
            } else if (useTick == stickChargeDuration - 11){
            }else if (useTick == stickChargeDuration){

                this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_RAY_CHARGE)
                        .build());
                this.entity.triggerPrepareParticle();
            } else if (useTick > rayStartTime + stickChargeDuration && useTick < rayStartTime + rayDuration + stickChargeDuration){
                this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_RAY_CAST)
                        .build());
                this.rotateToTarget(target);
                laserState = true;

                if (useTick % 3 == 0 && useTick < rayStartTime + rayDuration + stickChargeDuration - 15){
                    this.entity.level().playSound(null, this.entity.getX(), this.entity.getY(), this.entity.getZ(), RESounds.ENGINEER_RAY.get(), SoundSource.HOSTILE, 2f, 1f);
                }

                if (useTick % 2 == 0) {

                    Vec3 start = this.entity.getEyePosition();
                    Vec3 end = this.entity.getLaserTarget(1);
                    var damage = this.entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    for (var entity : FDHelpers.traceEntities(this.entity.level(), start, end, 0.1f, (entity) -> entity != this.entity)) {
                        entity.hurt(this.entity.level().damageSources().mobAttack(this.entity), (float) damage * 0.5f);
                    }
                }

            }else if (useTick == rayStartTime + rayDuration + stickChargeDuration){
                this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_RAY_CAST_STOP)
                        .build());
            }

            this.entity.setLaserState(laserState);

            useTick++;
        }



        private void rotateToTarget(LivingEntity target){
//            Vec3 position = target.position().add(0,target.getBbHeight() / 2, 0);
            Vec3 position = target.getEyePosition();

//            Vec3 thisPos = this.entity.position().add(0,this.entity.getBbHeight() / 2,0);
            Vec3 thisPos = this.entity.getEyePosition();
            Vec3 between = position.subtract(thisPos);

            var distance = between.length();
            float circleLength = (float) (distance * FDMathUtil.FPI * 2);
            float distancePerTick = 0.25f;
            float p = distancePerTick / circleLength;
            float anglePerTick = 360 * p;


            this.entity.getLookControl().setLookAt(target);

            this.entity.lookAt(target, anglePerTick,anglePerTick);
            float newYRot = this.entity.getYRot();
            float newXRot = this.entity.getXRot();

            Matrix4f mat = new Matrix4f();
            mat.rotateY((float) Math.toRadians(-newYRot + 180));
            mat.rotateX((float) Math.toRadians(-newXRot));

            Vec3 laserDirection = new Vec3(mat.transformDirection(new Vector3f(0,0,-1)));
            Vec3 laserEnd = thisPos.add(laserDirection.scale(30));

            ClipContext clipContext = new ClipContext(thisPos, laserEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            var result = this.entity.level().clip(clipContext);

            this.entity.setLaserTarget(result.getLocation());
            this.entity.setLaserState(true);
        }


        @Override
        public boolean canUse() {
            return !this.entity.isUsingLightningsAttack
                    && this.entity.random.nextFloat() < 0.05
                    && this.entity.lightningRayAttackCooldown == 0
                    && this.entity.getTarget() != null
                    && this.entity.getSensing().hasLineOfSight(this.entity.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return useTick < 140 && this.entity.getTarget() != null;
        }

    }

}
