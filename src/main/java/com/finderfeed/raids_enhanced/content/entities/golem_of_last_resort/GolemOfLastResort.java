package com.finderfeed.raids_enhanced.content.entities.golem_of_last_resort;

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
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.REUtil;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.content.entities.falling_block.REFallingBlock;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaiderBomb;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts.RaidBlimpPart;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class GolemOfLastResort extends FDRaider implements IHasHead<GolemOfLastResort>, AutoSerializable {

    public static final String MAIN_LAYER = "IDLE";
    public static final String WALKING_LAYER = "WALKING";
    public static final String WHIRLWIND_LAYER = "WHIRLWIND";

    private static FDModel clientModel;
    private static FDModel serverModel;

    @SerializableField
    private int golemBombsCooldown = 0;

    @SerializableField
    private int golemSpecialAttackCooldown = 0;

    private boolean isMeleeAttacking = false;
    private boolean isRangedAttacking = false;

    private boolean walkingWithHands = true;

    private int destroyBlocksTick;

    protected HeadControllerContainer<GolemOfLastResort> headControllerContainer;


    public GolemOfLastResort(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
        this.headControllerContainer = new HeadControllerContainer<>(this)
                .addHeadController(getModel(p_37840_), "head");
        this.headControllerContainer.setControllersMode(HeadControllerContainer.Mode.LOOK);
        this.lookControl = this.headControllerContainer;
        this.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_IDLE)
                .build());
        this.getAnimationSystem().setAnimationsApplyListener(this::onAnimationsApplied);
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));


        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new GolemBombsAttack(this));
        this.goalSelector.addGoal(4, new GolemMeleeAttackGoal(this, 2.5f, 2.5f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));

    }

    @Override
    public void applyRaidBuffs(int p_37844_, boolean p_37845_) {

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide){

            this.destroyBlocks();

            golemBombsCooldown = Mth.clamp(golemBombsCooldown - 1,0, Integer.MAX_VALUE);

            if (this.getTarget() != null) {
                if (this.distanceTo(this.getTarget()) < 3) {
                    golemSpecialAttackCooldown = Mth.clamp(golemSpecialAttackCooldown - 1, 0, Integer.MAX_VALUE);
                }
            }

            this.setYRot(this.yBodyRot);
            var animSystem = this.getAnimationSystem();

            double x = this.getLookControl().getWantedX();
            double y = this.getLookControl().getWantedY();
            double z = this.getLookControl().getWantedZ();

            Vec3 deltaMovement = this.getDeltaMovement().multiply(1,0,1);
            double speed = deltaMovement.length();

            if (x == 0 && y == 0 && z == 0) {
                this.getLookControl().setLookAt(this.getEyePosition().add(this.getLookAngle()));
            }

            var ticker = animSystem.getTicker(MAIN_LAYER);
            if (ticker != null) {
                var animation = ticker.getAnimation();
                if (speed > 0.01) {
                    if (this.getTarget() == null) {
                        this.getLookControl().setLookAt(this.getEyePosition().add(this.getLookAngle()));
                    }

                    if (this.isIdleAnim(animation) || animation.isToNullTransition()) {
                        this.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_WALK)
                                .build());
                    }

                    if (!this.walkingWithHands){
                        this.getAnimationSystem().startAnimation(WALKING_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_WALK_NO_HANDS)
                                        .setToNullTransitionTime(10)
                                .build());
                    }else{
                        this.getAnimationSystem().stopAnimation(WALKING_LAYER);
                    }

                } else {
                    this.getAnimationSystem().stopAnimation(WALKING_LAYER);
                    if (this.isWalkingAnim(animation) || animation.isToNullTransition()) {
                        animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_IDLE)
                                .build());
                    }
                }
            }else{
                animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_IDLE)
                        .build());
            }




        }else {
            if (!this.isDeadOrDying()) {
                this.getHeadControllerContainer().clientTick();
            }else{
                this.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.ANIMATION);
            }
        }
    }

    @Override
    protected void tickDeath() {

        if (!level().isClientSide){
            if (deathTime == 0){
                this.getAnimationSystem().stopAnimation(MAIN_LAYER);
                this.getAnimationSystem().stopAnimation(WALKING_LAYER);
                this.getAnimationSystem().stopAnimation(WHIRLWIND_LAYER);

                this.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_DEATH).build());
            }else if (this.deathTime >= 20){
                this.spawnDeathParts();
                this.explode();
            }
        }

        this.deathTime++;
    }



    private void explode(){
        level().playSound(null, this.position().x,this.position().y,this.position().z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5f, 1f);
        Vec3 pos = this.position();
        for (var serverPlayer : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, level(), pos, 160)) {
            for (int i = 0; i < 2; i++){
                Vec3 ppos = this.position().add(
                        random.nextFloat() * 2 - 1,
                        0.25 + random.nextFloat() * 1,
                        random.nextFloat() * 2 - 1
                );
                ((ServerLevel) level()).sendParticles(serverPlayer, ParticleTypes.EXPLOSION_EMITTER, true, ppos.x, ppos.y, ppos.z, 1, 0, 0, 0, 0);
            }
        }
        this.setRemoved(RemovalReason.DISCARDED);
    }

    private void spawnDeathParts(){

        for (int i = 0; i < 10; i++){

            Vec3 v = new Vec3(1,0,0).yRot(random.nextFloat() * FDMathUtil.FPI * 2);
            Vec3 speed = v.scale(0.1f + random.nextFloat() * 1).add(0,0.15 + random.nextFloat() * 1.25f,0);
            Vec3 startPos = this.position().add(0,1 + random.nextFloat(),0).add(v.scale(random.nextFloat() * 3f));
            RaidBlimpPart.summon(level(), startPos, speed, RaidBlimpPart.GOLEM_PART, random.nextInt(100) + 100);

        }

    }

    @Override
    public float getEyeHeight(Pose p_20237_) {
        return 2.05f;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return RESounds.ILLAGER_GOLEM_DEATH.get();
    }

    private void destroyBlocks() {
        if (this.destroyBlocksTick > 0) {
            this.destroyBlocksTick--;
            if (this.destroyBlocksTick == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                boolean flag = false;
                int l = Mth.floor(this.getBbWidth() / 2.0F + 1.5F);
                int i1 = Mth.floor(this.getBbHeight());

                for (BlockPos blockpos : BlockPos.betweenClosed(this.getBlockX() - l, this.getBlockY(), this.getBlockZ() - l, this.getBlockX() + l, this.getBlockY() + i1, this.getBlockZ() + l)) {

                    Vec3 center = blockpos.getCenter();
                    Vec3 pos = this.position();
                    Vec3 between = pos.subtract(center).multiply(1,0,1);
                    if (between.length() < 2.5) {
                        BlockState blockstate = this.level().getBlockState(blockpos);
                        if (blockstate.canEntityDestroy(this.level(), blockpos, this) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(this, blockpos, blockstate)) {
                            flag = this.level().destroyBlock(blockpos, true, this) || flag;
                        }
                    }
                }

                if (flag) {
                    this.level().levelEvent(null, 1022, this.blockPosition(), 0);
                }
            }
        }
    }


    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }


    private void onAnimationsApplied(FDModel model, Float partialTicks) {
        var system = this.getAnimationSystem();
        var ticker = system.getTicker(WHIRLWIND_LAYER);
        float strength = 1f;
        if (ticker != null) {
            float time = ticker.getTime(partialTicks);
            var animation = ticker.getAnimation();
            float animTime = animation.getAnimTime();
            float p = time / animTime;
            if (p < 0.25f){
                strength = 1 - p / 0.25f;
            }else if (p >= 0.75f) {
                strength = (p - 0.75f) / 0.25f;
            }else{
                strength = 0;
            }
        }
        strength = Mth.clamp(strength,0,1);
        this.getAnimationSystem().setVariable("variable.hand_strength", strength);
    }


    @Override
    public boolean hurt(DamageSource src, float damage) {

        var entity = src.getEntity();
        if (entity == null || entity.distanceTo(this) < 10) {
            if (super.hurt(src, damage)) {
                this.destroyBlocksTick = 5;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource src) {
        return super.isInvulnerableTo(src) || src.is(DamageTypes.MOB_PROJECTILE) || src.is(DamageTypes.ARROW);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.autoSave(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.autoLoad(tag);
    }

    public boolean isIdleAnim(Animation animation){
        if (animation == null){
            return false;
        }else if (animation instanceof TransitionAnimation transitionAnimation){
            return transitionAnimation.getTransitionTo() == REAnimations.ILLAGER_GOLEM_IDLE.get();
        }else{
            return animation == REAnimations.ILLAGER_GOLEM_IDLE.get();
        }
    }

    public boolean isWalkingAnim(Animation animation){
        if (animation == null){
            return false;
        }else if (animation instanceof TransitionAnimation transitionAnimation){
            return transitionAnimation.getTransitionTo() == REAnimations.ILLAGER_GOLEM_WALK.get();
        }else{
            return animation == REAnimations.ILLAGER_GOLEM_WALK.get();
        }
    }

    public static FDModel getModel(Level level){
        if (!level.isClientSide){
            if (serverModel == null){
                serverModel = new FDModel(REModels.GOLEM_OF_LAST_RESORT.get());
            }
            return serverModel;
        }else{
            if (clientModel == null){
                clientModel = new FDModel(REModels.GOLEM_OF_LAST_RESORT.get());
            }
            return clientModel;
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    @Override
    public HeadControllerContainer<GolemOfLastResort> getHeadControllerContainer() {
        return this.headControllerContainer;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33034_) {
        return RESounds.RAID_GOLEM_HIT.get();
    }

    public static class GolemBombsAttack extends Goal {

        public int attackTime;
        public GolemOfLastResort golem;


        public GolemBombsAttack(GolemOfLastResort golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            return this.golem.getTarget() != null && this.golem.onGround() && this.golem.golemBombsCooldown <= 0 && this.golem.random.nextFloat() < 0.05 && !this.golem.isMeleeAttacking
                    && (this.golem.getTarget().distanceTo(this.golem) > 4 || Math.abs(this.golem.getY() - this.golem.getTarget().getY()) > 2);
        }

        @Override
        public void start() {
            super.start();
            this.golem.isMeleeAttacking = false;
            this.golem.isRangedAttacking = true;
            attackTime = 0;
        }

        @Override
        public void tick() {
            super.tick();
            var target = this.golem.getTarget();

            if (target == null){
                this.golem.isRangedAttacking = false;
                return;
            }

            var system = this.golem.getAnimationSystem();

            this.golem.getNavigation().stop();


            this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.ANIMATION);

            if (this.attackTime == 0){
                this.golem.setDeltaMovement(Vec3.ZERO);
                system.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_BOMBS)
                        .build());


            } else if (this.attackTime == 12){

                Matrix4f t = this.golem.getModelPartTransformation(this.golem,"cannon", getModel(golem.level()));
                Vec3 pos = new Vec3(t.transformPosition(new Vector3f())).add(this.golem.position());

                for (var player : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, target.level(), pos, 120)){
                    ((ServerLevel)golem.level()).sendParticles(player, new SimpleTexturedParticleOptions(REParticles.EXPLOSION.get(),1.5f,9), true, pos.x, pos.y + 0.75, pos.z,1,0,0,0,0);
                }
                ((ServerLevel)golem.level()).playSound(null, pos.x,pos.y,pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f, 1.5f);
                ((ServerLevel)golem.level()).playSound(null, pos.x,pos.y,pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f, 0.75f);

                List<Player> targets = FDTargetFinder.getEntitiesInCylinder(Player.class, golem.level(), golem.position().add(0,-20,0), 40,20);

                for (var tr : targets){
                    if (tr.isSpectator() || tr.isCreative()) continue;
                    this.launchBombAtTarget(tr, pos);
                }

                if (!targets.contains(target)){
                    this.launchBombAtTarget(target, pos);
                }

            } else if (attackTime > 25){
                this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.LOOK);
                this.golem.golemBombsCooldown = 100;
                this.golem.isRangedAttacking = false;
            } else if (attackTime == 9){
                ((ServerLevel)golem.level()).playSound(null, this.golem.getX(), this.golem.getY() + 1, this.golem.getZ(), RESounds.RAID_GOLEM_PREPARE_PUNCH.get(), SoundSource.HOSTILE, 2f, 0.75f);
                ((ServerLevel)golem.level()).playSound(null, this.golem.getX(), this.golem.getY() + 1, this.golem.getZ(), RESounds.RAID_GOLEM_PREPARE_PUNCH.get(), SoundSource.HOSTILE, 2f, 1.25f);
            }
            this.attackTime++;
        }

        private void launchBombAtTarget(LivingEntity target, Vec3 bombStartPos){
            Vec3 speed = REUtil.calculateMortarProjectileVelocity(bombStartPos, target.position(), -ServerPlayer.DEFAULT_BASE_GRAVITY, 20 + golem.random.nextInt(5));
            RaiderBomb.summon(this.golem, bombStartPos, speed);
        }

        @Override
        public void stop() {
            this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.LOOK);
            this.golem.getNavigation().stop();
            this.golem.getLookControl().setLookAt(this.golem.getEyePosition().add(this.golem.getLookAngle()));
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.getTarget() != null && this.golem.golemBombsCooldown <= 0 && this.golem.isRangedAttacking;
        }

    }



    public static class GolemMeleeAttackGoal extends Goal {

        //0 - default
        //1 - heavy attack
        //2 - whirlwind
        public int meleeAttackType = 0;

        public int attackTime = 0;

        public GolemOfLastResort golem;

        public float golemAttackHeight;
        public float horizontalAttackRange;

        public GolemMeleeAttackGoal(GolemOfLastResort golem, float golemAttackHeight, float horizontalAttackRange) {
            this.golem = golem;
            this.golemAttackHeight = golemAttackHeight;
            this.horizontalAttackRange = horizontalAttackRange;
        }

        @Override
        public boolean canUse() {
            return this.golem.getTarget() != null && !this.golem.isRangedAttacking;
        }

        @Override
        public void start() {
            attackTime = 0;
        }

        public boolean isTargetInAttackRange(LivingEntity target){
            Vec3 between = target.position().subtract(golem.position());
            double yDiff = between.y;
            if (yDiff >= -1 && yDiff <= golemAttackHeight){
                double horizontalDistance = between.multiply(1,0,1).length();
                return horizontalDistance < horizontalAttackRange;
            }
            return false;
        }

        @Override
        public void tick() {
            if (meleeAttackType > 2){
                this.meleeAttackType = 0;
            }
            var target = this.golem.getTarget();
            if (target != null){
                if (attackTime <= 0) {
                    this.golem.isMeleeAttacking = false;

                    if (this.isTargetInAttackRange(target)) {
                        this.golem.getLookControl().setLookAt(target);
                        this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, target.position());
                        this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.ANIMATION);

                        if (this.golem.golemSpecialAttackCooldown > 0){
                            meleeAttackType = 0;
                        }else{
                            this.golem.golemSpecialAttackCooldown = 60 + golem.random.nextInt(20);
                            meleeAttackType = golem.random.nextInt(2) + 1;
                        }

                        this.startAnimationAndAttackTicker(this.meleeAttackType);
                    }else {

                        var movement = this.golem.getDeltaMovement();
                        Vec3 between = target.position().subtract(this.golem.position()).multiply(1,0,1);
                        if (!between.equals(Vec3.ZERO) && between.length() > 0.5){
                            Vec3 addition = between.normalize().scale(0.01f);
                            this.golem.setDeltaMovement(movement.add(addition));
                        }

                        this.golem.walkingWithHands = true;
                        this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.LOOK);
                        this.golem.getLookControl().setLookAt(target);
                        var navigation = this.golem.getNavigation();
                        navigation.moveTo(target.getX(),target.getY(),target.getZ(), 1.2f);
                    }

                }else{
                    this.golem.isMeleeAttacking = true;

                    attackTime--;

                    if (this.meleeAttackType == 0){
                        this.defaultMeleeAttack();
                    }else if (this.meleeAttackType == 1){
                        this.heavyAttack();
                    }else if (this.meleeAttackType == 2){
                        this.whirlwindAttack();
                    }

                }
            }
        }

        private void whirlwindAttack(){

            this.golem.walkingWithHands = true;
            var target = this.golem.getTarget();
            this.golem.getNavigation().stop();
            if (target != null && attackTime < 16){
                var movement = this.golem.getDeltaMovement();
                Vec3 between = target.position().subtract(this.golem.position()).multiply(1,0,1);
                if (!between.equals(Vec3.ZERO) && between.length() > 0.5){
                    Vec3 addition = between.normalize().scale(0.1f);
                    this.golem.setDeltaMovement(movement.add(addition));
                }
                this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, target.position());
            }
            if (attackTime > 10 && attackTime < 16){
                 this.golem.level().playSound(null, golem.getX(),golem.getY(),golem.getZ(), RESounds.RAID_GOLEM_SWING.get(), SoundSource.HOSTILE, 1f, 1.1f + golem.random.nextFloat() * 0.2f);

                var targets = FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, this.golem.level(), this.golem.position().add(0,-1,0), golemAttackHeight + 1, horizontalAttackRange);
                for (var t : targets) {
                    if (t != this.golem) {
                        this.golem.doHurtTarget(t);
                        if (t instanceof Player player && player.getUseItem().getItem() instanceof ShieldItem){
                            player.disableShield(true);
                        }
                    }
                }
            }else if (attackTime == 19){
                this.golem.level().playSound(null, golem.getX(),golem.getY(),golem.getZ(), RESounds.RAID_GOLEM_PREPARE_PUNCH.get(), SoundSource.HOSTILE, 1f, 0.9f);
            }
        }


        private void heavyAttack(){
            this.golem.walkingWithHands = false;
            this.golem.getLookControl().setLookAt(this.golem.getTarget());
            this.golem.getNavigation().stop();
            this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, this.golem.getTarget().position());
            Vec3 smackPos = golem.position().add(golem.getForward().scale(1.5f));
            if (attackTime == 10){
                this.golem.level().playSound(null, smackPos.x, smackPos.y, smackPos.z, RESounds.RAID_GOLEM_HEAVY_STRKE.get(), SoundSource.HOSTILE, 2f, 0.8f + golem.random.nextFloat() * 0.2f);
                REUtil.golemSmackParticles((ServerLevel) golem.level(), smackPos, 60);

                BlockPos basePos = new BlockPos(
                        (int) Math.floor(smackPos.x),
                        (int) Math.floor(smackPos.y + 0.1f) - 1,
                        (int) Math.floor(smackPos.z)
                );

                for (int x = -1; x <= 1; x++){
                    for (int z = -1; z <= 1; z++){

                        if (Math.abs(x + z) > 1 && golem.random.nextFloat() > 0.5){
                            continue;
                        }

                        BlockPos pos = basePos.offset(x,0,z);
                        BlockState blockState = golem.level().getBlockState(pos);
                        Vec3 summonPos = pos.getCenter();
                        Vec3 between = summonPos.subtract(smackPos).multiply(1,0,1);

                        Vec3 speed = between.normalize().yRot(-FDMathUtil.FPI / 8 + golem.level().random.nextFloat() * FDMathUtil.FPI / 4)
                                .scale(0.05f + golem.random.nextFloat() * 0.1f)
                                .add(0,0.2 + golem.level().random.nextFloat() * 0.2f, 0);

                        REFallingBlock fallingBlock = REFallingBlock.summon(golem.level(), blockState, pos.getCenter(), speed, (float) ServerPlayer.DEFAULT_BASE_GRAVITY);

                    }
                }


                PositionedScreenShakePacket.send((ServerLevel) golem.level(), FDShakeData.builder()
                        .frequency(5f)
                        .amplitude(5f)
                        .inTime(0)
                        .stayTime(0)
                        .outTime(6)
                        .build(),smackPos,10);


                Vec3 forward = this.golem.getForward();

                Vec2 direction = new Vec2(
                        (float)forward.x,
                        (float)forward.z
                );
                var targets = FDTargetFinder.getEntitiesInArc(LivingEntity.class, this.golem.level(), this.golem.position().add(0,-1,0), direction, FDMathUtil.FPI, golemAttackHeight + 1,
                        horizontalAttackRange);
                for (var t : targets) {
                    if (t != this.golem) {
                        this.golem.doHurtTarget(t);
                        if (t instanceof Player player && player.getUseItem().getItem() instanceof ShieldItem){
                            player.disableShield(true);
                        }
                    }
                }
            }else if (attackTime == 16){
                this.golem.level().playSound(null, golem.getX(),golem.getY(),golem.getZ(), RESounds.RAID_GOLEM_PREPARE_PUNCH.get(), SoundSource.HOSTILE, 2f, 0.9f + golem.random.nextFloat() * 0.2f);
            }else if (attackTime == 12){
                this.golem.level().playSound(null, golem.getX(),golem.getY(),golem.getZ(), RESounds.RAID_GOLEM_SWING.get(), SoundSource.HOSTILE, 1f, 1.1f + golem.random.nextFloat() * 0.2f);
            }
        }

        private void defaultMeleeAttack(){
            this.golem.walkingWithHands = false;
            this.golem.getLookControl().setLookAt(this.golem.getEyePosition().add(this.golem.getLookAngle()));
            this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, this.golem.getTarget().position());
            this.golem.getNavigation().stop();

            if (attackTime == 7){

                Vec3 forward = this.golem.getForward();

                Vec2 direction = new Vec2(
                        (float)forward.x,
                        (float)forward.z
                );
                var targets = FDTargetFinder.getEntitiesInArc(LivingEntity.class, this.golem.level(), this.golem.position().add(0,-1,0), direction, FDMathUtil.FPI, golemAttackHeight + 1,
                        horizontalAttackRange);
                for (var t : targets) {
                    if (t != this.golem) {
                        this.golem.doHurtTarget(t);
                    }
                }
            }else if (attackTime == 9){
                this.golem.level().playSound(null, golem.getX(),golem.getY(),golem.getZ(), RESounds.RAID_GOLEM_SWING.get(), SoundSource.HOSTILE, 1f, 1.1f + golem.random.nextFloat() * 0.2f);
            }
        }

        private void startAnimationAndAttackTicker(int attackType){
            if (attackType == 0){
                Animation animation = this.golem.random.nextBoolean() ? REAnimations.ILLAGER_GOLEM_STRIKE_1.get() : REAnimations.ILLAGER_GOLEM_STRIKE_2.get();
                this.golem.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(animation)
                        .important()
                        .setSpeed(1f)
                        .build());
                this.golem.level().playSound(null, golem.getX(),golem.getY(),golem.getZ(), RESounds.RAID_GOLEM_PREPARE_PUNCH.get(), SoundSource.HOSTILE, 1f, 0.9f + golem.random.nextFloat() * 0.2f);
                attackTime = 10;
            }else if (attackType == 1){
                this.golem.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_HEAVY_STRIKE)
                        .important()
                                .setSpeed(0.95f)
                        .build());
                attackTime = 17;
            }else{
                this.golem.getAnimationSystem().startAnimation(WHIRLWIND_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_WHIRLWIND)
                                .setToNullTransitionTime(0)
                        .build());
                attackTime = 20;
            }
        }

        @Override
        public void stop() {
            this.golem.walkingWithHands = true;
            this.golem.isMeleeAttacking = false;
            this.golem.getNavigation().stop();
            this.golem.getLookControl().setLookAt(this.golem.getEyePosition().add(this.golem.getLookAngle()));
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.getTarget() != null && !this.golem.isRangedAttacking;
        }

    }

}
