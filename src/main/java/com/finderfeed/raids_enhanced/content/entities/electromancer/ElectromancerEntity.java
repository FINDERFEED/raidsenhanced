package com.finderfeed.raids_enhanced.content.entities.electromancer;

import com.finderfeed.fdlib.init.FDEDataSerializers;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.TransitionAnimation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.HeadControllerContainer;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.client.particles.BoneAttachedParticles;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import com.finderfeed.raids_enhanced.init.REParticles;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ElectromancerEntity extends FDRaider {


    public static final String MAIN_LAYER = "IDLE";
    public static final String WALKING_LAYER = "WALKING";

    public static final String LIGHTNING_START = "lightning_start";

    public static final EntityDataAccessor<Byte> BYTE_PARTICLE_TRIGGER = SynchedEntityData.defineId(ElectromancerEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> LASER_ACTIVE = SynchedEntityData.defineId(ElectromancerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vec3> LASER_TARGET = SynchedEntityData.defineId(ElectromancerEntity.class, FDEDataSerializers.VEC3.get());

    private Vec3 laserTargetOld;

    private static FDModel clientModel;
    private static FDModel serverModel;

    private BoneAttachedParticles boneAttachedParticles;

    public ElectromancerEntity(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
        this.lookControl = new LookControl(this) {
            @Override
            protected boolean resetXRotOnTick() {
                return !((ElectromancerEntity)this.mob).isLaserActive();
            }
        };
        if (level().isClientSide){
            this.boneAttachedParticles = new BoneAttachedParticles(getModel(level()), this, LIGHTNING_START, Vec3.ZERO);
        }
        this.getModelSystem().getAnimationSystem().setAnimationsApplyListener(this::onAnimationsApplied);
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


        this.goalSelector.addGoal(4, new LaserAttackGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide){
            this.controlIdleAndWalking();
        }else{

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

    protected float rotateTowards(float p_24957_, float p_24958_, float p_24959_) {
        float f = Mth.degreesDifference(p_24957_, p_24958_);
        float f1 = Mth.clamp(f, -p_24959_, p_24959_);
        return p_24957_ + f1;
    }

    public static class LaserAttackGoal extends Goal {

        private ElectromancerEntity entity;

        private int useTick = 0;

        public LaserAttackGoal(ElectromancerEntity electromancerEntity){
            this.entity = electromancerEntity;
        }

        @Override
        public void start() {
            super.start();
            useTick = 0;
        }

        @Override
        public void tick() {
            super.tick();
            var target = entity.getTarget();
            if (target == null) return;

            this.entity.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ELECTROMANCER_RAY_CAST).build());

            this.entity.getNavigation().stop();


            Vec3 position = target.position().add(0,target.getBbHeight() / 2, 0);

            Vec3 thisPos = this.entity.position().add(0,this.entity.getBbHeight() / 2,0);
            Vec3 between = position.subtract(thisPos);

//            float yRot = this.entity.getYRot();
//            float targetYRot = (float) (Mth.atan2(between.z, between.x) * 180.0F / (float) Math.PI) - 90.0F;
//
            var distance = between.length();
            float circleLength = (float) (distance * FDMathUtil.FPI * 2);
            float distancePerTick = 0.5f;
            float p = distancePerTick / circleLength;
            float anglePerTick = 360 * p;


//            float newYRot = this.entity.rotateTowards(yRot, targetYRot, anglePerTick);
//
//            double d4 = Math.sqrt(between.x * between.x + between.z * between.z);
//            float targetXRot = (float) (-(Mth.atan2(between.y, d4) * 180.0F / (float) Math.PI));
//            float currentXRot = this.entity.getXRot();
//            float newXRot = this.entity.rotateTowards(currentXRot, targetXRot, anglePerTick);
//            this.entity.setYRot(newYRot);
//            this.entity.setXRot(newXRot);


            this.entity.getLookControl().setLookAt(target);

            this.entity.lookAt(target, anglePerTick,anglePerTick);
            float newYRot = this.entity.getYRot();
            float newXRot = this.entity.getXRot();

            Matrix4f mat = new Matrix4f();
            mat.rotateY((float) Math.toRadians(-newYRot + 180));
            mat.rotateX((float) Math.toRadians(-newXRot));

            Vec3 laserDirection = new Vec3(mat.transformDirection(new Vector3f(0,0,-1)));
            Vec3 laserEnd = thisPos.add(laserDirection.scale(30));

            this.entity.setLaserTarget(laserEnd);
            this.entity.setLaserState(true);

        }


        @Override
        public boolean canUse() {
            return this.entity.getTarget() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return useTick < 100 && this.entity.getTarget() != null;
        }

    }

}
