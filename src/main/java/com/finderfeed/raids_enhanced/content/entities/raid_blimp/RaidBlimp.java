package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons.RaidBlimpCannonsController;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.navigation.RaidBlimpMoveControl;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.navigation.RaidBlimpPathNavigation;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts.RaidBlimpPart;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RaidBlimp extends FDRaider {

    private static FDModel clientModel;
    private static FDModel serverModel;

    private Entity lastHurtBy;

    public static final String BOMB_ILLAGER_LAYER = "ropes_bombs_its_yours_my_friend";

    private static final EntityDataAccessor<Integer> targetRight1 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetRight2 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetRight3 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetLeft1 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetLeft2 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetLeft3 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);

    public RaidBlimpCannonsController cannonsController;

    private int bombThrowTicker = -1;
    private Vec3 bombThrowPos = null;

    //death
    private Vec3 randomFallingDirection;
    private float fallingPower = 1f;

    public RaidBlimp(EntityType<? extends FDRaider> type, Level level) {
        super(type, level);
        this.cannonsController = new RaidBlimpCannonsController(this,
                targetRight1,targetRight2,targetRight3,
                targetLeft1,targetLeft2,targetLeft3
        );
        this.moveControl = new RaidBlimpMoveControl(this, 10, false);
        this.getAnimationSystem().startAnimation("IDLE", AnimationTicker.builder(REAnimations.RAID_AIRSHIP_FLY.get())
                .build());
        this.getAnimationSystem().startAnimation("IDLE2", AnimationTicker.builder(REAnimations.RAID_AIRSHIP_IDLE.get())
                .build());
        this.getAnimationSystem().startAnimation(BOMB_ILLAGER_LAYER, AnimationTicker.builder(REAnimations.RAID_AIRSHIP_ILLAGER_OBSERVE.get())
                        .setToNullTransitionTime(0)
                .build());
        this.setNoGravity(true);

    }

    public static FDModel getModel(RaidBlimp raidBlimp){
        if (raidBlimp.level().isClientSide){
            if (clientModel == null){
                clientModel = new FDModel(REModels.RAID_BLIMP.get());
            }
            return clientModel;
        }else{
            if (serverModel == null){
                serverModel = new FDModel(REModels.RAID_BLIMP.get());
            }
            return serverModel;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (!this.isDeadOrDying()) {
                this.detectEntitiesBeneathAndThrowBomb();
            }
            this.turnToLastHurtBy();
        }

        if (!this.isDeadOrDying()) {
            this.cannonsController.tick();
        }
    }

    private void turnToLastHurtBy(){
//        lastHurtBy = null;
        if (this.isDeadOrDying()) return;

        if (this.getLastHurtBy() != null){

            var entity = this.getLastHurtBy();
            Vec3 pos = entity.position();


            double d0 = pos.x - this.getX();
            double d1 = pos.y - this.getY();
            double d2 = pos.z - this.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d3 >= 2.5000003E-7F) {
                float f = (float)(Mth.atan2(d2, d0) * 180.0F / (float)Math.PI);
                this.setYRot(this.rotlerp(this.getYRot(), f, 2));
            }


            Vec3 b = pos.subtract(this.position()).multiply(1,0,1).normalize();
            Vec3 l = this.getLookAngle().multiply(1,0,1).normalize();
            double angle = FDMathUtil.angleBetweenVectors(b,l);
            if (angle >= FDMathUtil.FPI / 2 - FDMathUtil.FPI / 16 && angle <= FDMathUtil.FPI / 2 + FDMathUtil.FPI / 16){
                lastHurtBy = null;
            }

        }
    }

    protected float rotlerp(float p_24992_, float p_24993_, float p_24994_) {
        float f = Mth.wrapDegrees(p_24993_ - p_24992_);
        if (f > p_24994_) {
            f = p_24994_;
        }

        if (f < -p_24994_) {
            f = -p_24994_;
        }

        float f1 = p_24992_ + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }

    public void setLastHurtBy(Entity lastHurtBy) {
        this.lastHurtBy = lastHurtBy;
    }

    public Entity getLastHurtBy() {
        return lastHurtBy;
    }

    @Override
    public boolean hurt(DamageSource src, float damage) {
        boolean result;
        if (result = super.hurt(src, damage) && src.getEntity() != null){

            var entity = src.getEntity();
            Vec3 pos = entity.position();
            Vec3 relativePos = pos.subtract(this.position());
            Vec3 rotated = relativePos.yRot((float) Math.toRadians(this.getYRot()));

            if (Math.abs(rotated.x) < 3) {
                this.setLastHurtBy(src.getEntity());
            }
        }
        return result;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.level().isClientSide()) {

            if (deathTime == 1){
                level().playSound(null, this.position().x,this.position().y,this.position().z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5f, 1f);
                level().playSound(null, this.position().x,this.position().y,this.position().z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5f, 0.5f);
                level().playSound(null, this.position().x,this.position().y,this.position().z, RESounds.RAID_BLIMP_FALL.get(), SoundSource.HOSTILE, 5f, 1.1f);
            }

            this.getAnimationSystem().stopAnimation(BOMB_ILLAGER_LAYER);
            this.getAnimationSystem().startAnimation("IDLE", AnimationTicker.builder(REAnimations.RAID_AIRSHIP_DEATH)
                    .build());

            if (randomFallingDirection == null){
                fallingPower = 1f;
                randomFallingDirection = new Vec3(1,0,0).yRot(random.nextFloat() * FDMathUtil.FPI * 2);
            }

            fallingPower = Mth.clamp(fallingPower - 0.05f,-0.25f,1);

            Vec3 deltaMovement = this.getDeltaMovement();
            Vec3 newDeltaMovement = new Vec3(
                    deltaMovement.x + randomFallingDirection.x * fallingPower * 0.1,
                    -0.8,
                    deltaMovement.z + randomFallingDirection.z * fallingPower * 0.1
            );

            this.setDeltaMovement(newDeltaMovement);

            if (this.onGround() || this.deathTime > 300){
                this.spawnDeathParts();
                this.explode();
            }

        }else{

            this.deathClientParticles();

        }
    }

    private void spawnDeathParts(){

        for (int i = 0; i < 30; i++){

            Vec3 v = new Vec3(1,0,0).yRot(random.nextFloat() * FDMathUtil.FPI * 2);
            Vec3 speed = v.scale(0.1f + random.nextFloat() * 1).add(0,0.15 + random.nextFloat() * 1.25f,0);
            Vec3 startPos = this.position().add(v.scale(random.nextFloat() * 3f));

            RaidBlimpPart.summon(level(), startPos, speed, i == 0 ? RaidBlimpPart.PROPELLER : RaidBlimpPart.WOODEN_STICK, random.nextInt(200) + 400);

        }

    }

    private void deathClientParticles(){
        if (deathTime == 1){
            for (int i = 0; i < 3; i++){
                Vec3 ppos = this.position().add(
                        random.nextFloat() * 5 - 2.5,
                        random.nextFloat() * 4,
                        random.nextFloat() * 5 - 2.5
                );
                level().addParticle(ParticleTypes.EXPLOSION_EMITTER, true, ppos.x, ppos.y, ppos.z, 0,0,0);
            }

        }

        for (int i = 0; i < 10; i ++){
            Vec3 ppos = this.position().add(
                    random.nextFloat() * 4 - 2,
                    random.nextFloat() * 4,
                    random.nextFloat() * 4 - 2
            );
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, ppos.x, ppos.y, ppos.z, random.nextFloat() * 0.2f - 0.1f, random.nextFloat() * 0.2f - 0.1f, random.nextFloat() * 0.2f - 0.1f);
        }

        for (int i = 0; i < 10; i ++){
            Vec3 ppos = this.position().add(
                    random.nextFloat() * 4 - 2,
                    random.nextFloat() * 4,
                    random.nextFloat() * 4 - 2
            );
            level().addParticle(ParticleTypes.LARGE_SMOKE, true, ppos.x, ppos.y, ppos.z, random.nextFloat() * 0.2f - 0.1f, random.nextFloat() * 0.2f - 0.1f, random.nextFloat() * 0.2f - 0.1f);
        }
    }


    private void explode(){
        level().playSound(null, this.position().x,this.position().y,this.position().z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5f, 1f);
        level().playSound(null, this.position().x,this.position().y,this.position().z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5f, 0.5f);
        Vec3 pos = this.position();
        for (var serverPlayer : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, level(), pos, 160)) {
            for (int i = 0; i < 5; i++){
                Vec3 ppos = this.position().add(
                        random.nextFloat() * 8 - 4,
                        random.nextFloat() * 4,
                        random.nextFloat() * 8 - 4
                );
                ((ServerLevel) level()).sendParticles(serverPlayer, ParticleTypes.EXPLOSION_EMITTER, true, ppos.x, ppos.y, ppos.z, 1, 0, 0, 0, 0);
            }
        }
        this.setRemoved(RemovalReason.DISCARDED);
    }

    private void detectEntitiesBeneathAndThrowBomb(){
        if (bombThrowTicker >= 0){

            if (bombThrowTicker > 10){
                this.getAnimationSystem().startAnimation(BOMB_ILLAGER_LAYER, AnimationTicker.builder(REAnimations.RAID_AIRSHIP_THROW_BOMB)
                                .setToNullTransitionTime(0)
                                .setLoopMode(Animation.LoopMode.HOLD_ON_LAST_FRAME)
                        .build());
            }

            if (bombThrowTicker == 13){
                Matrix4f mat = this.getModelPartTransformation(this,"pillager_bomb", getModel(this), 1);
                Vec3 t = new Vec3(mat.transformPosition(new Vector3f(0,0,0))).add(this.position());
                if (bombThrowPos == null || bombThrowPos.distanceTo(this.position()) > 200){
                    bombThrowPos = this.position().add(0,-40,0);
                }
                Vec3 throwPos = bombThrowPos.add(
                        level().random.nextGaussian(),
                        0,
                        level().random.nextGaussian()
                );
                double startSpeed = -1.5f;
                Vec3 hb = this.calculateBombHorizontalSpeed(t, throwPos, startSpeed, ServerPlayer.DEFAULT_BASE_GRAVITY);
                Vec3 speed = new Vec3(hb.x,startSpeed,hb.z);
                RaidBlimpBomb.summon(this, t, speed);
            }

            bombThrowTicker--;
        }else{
            this.getAnimationSystem().startAnimation(BOMB_ILLAGER_LAYER, AnimationTicker.builder(REAnimations.RAID_AIRSHIP_ILLAGER_OBSERVE)
                    .setToNullTransitionTime(0)
                    .build());
            if (tickCount % 5 == 0) {
                float downDistance = 40;
                var targets = FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, level(), this.position().add(0, -downDistance, 0), downDistance, 5, entity -> {
                    ClipContext clipContext = new ClipContext(this.position(), entity.position().add(0, entity.getBbHeight() / 2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
                    var res = level().clip(clipContext);
                    return entity != this && res.getType() == HitResult.Type.MISS;
                });
                if (!targets.isEmpty()) {
                    this.bombThrowPos = targets.get(level().random.nextInt(targets.size())).position();
                    this.bombThrowTicker = REAnimations.RAID_AIRSHIP_THROW_BOMB.get().getAnimTime();
                }
            }
        }
    }

    private Vec3 calculateBombHorizontalSpeed(Vec3 startPos, Vec3 endPos, double startVerticalSpeed, double gravity){
        double dist = startPos.y - endPos.y;
        if (dist < 0){
            return endPos.subtract(startPos).multiply(1,0,1).scale(0.1f);
        }
        if (gravity > 0){
            gravity = -gravity;
        }
        if (startVerticalSpeed > 0){
            startVerticalSpeed = -startVerticalSpeed;
        }

        double d = startVerticalSpeed * startVerticalSpeed - 4 * gravity * dist;
        double time = (-startVerticalSpeed - Math.sqrt(d)) / (2 * gravity);

        if (time <= 0){
            return endPos.subtract(startPos).multiply(1,0,1).scale(0.1f);
        }

        double hdist = Math.sqrt(Math.pow(endPos.x - startPos.x,2) + Math.pow(endPos.z - startPos.z,2));
        double hspeed = hdist / time;

        return endPos.subtract(startPos).multiply(1,0,1).normalize().scale(hspeed);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }


    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    protected PathNavigation createNavigation(Level p_186262_) {
        RaidBlimpPathNavigation flyingpathnavigation = new RaidBlimpPathNavigation(this, p_186262_);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(targetRight1, -1);
        builder.define(targetRight2, -1);
        builder.define(targetRight3, -1);
        builder.define(targetLeft1 , -1);
        builder.define(targetLeft2 , -1);
        builder.define(targetLeft3 , -1);
    }

    @Override
    public void applyRaidBuffs(ServerLevel level, int p_37844_, boolean p_37845_) {

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    @Override
    public void push(double p_20286_, double p_20287_, double p_20288_) {

    }

    @Override
    public void push(Vec3 p_347665_) {

    }

    @Override
    public void push(Entity p_21294_) {

    }

    @Override
    protected void pushEntities() {

    }

    @Override
    public void knockback(double p_147241_, double p_147242_, double p_147243_) {

    }

    @Override
    protected void playBlockFallSound() {

    }

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {

    }

    @Override
    public boolean causeFallDamage(float p_147187_, float p_147188_, DamageSource p_147189_) {
        return false;
    }

}
