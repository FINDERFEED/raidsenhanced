package com.finderfeed.raids_enhanced.content.entities.golem_of_last_resort;

import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.TransitionAnimation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.HeadControllerContainer;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.IHasHead;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class GolemOfLastResort extends FDRaider implements IHasHead<GolemOfLastResort> {

    public static final String MAIN_LAYER = "IDLE";

    private static FDModel clientModel;
    private static FDModel serverModel;

    protected HeadControllerContainer<GolemOfLastResort> headControllerContainer;

    public GolemOfLastResort(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
        this.headControllerContainer = new HeadControllerContainer<>(this)
                .addHeadController(getModel(p_37840_), "head");
        this.headControllerContainer.setControllersMode(HeadControllerContainer.Mode.LOOK);
        this.lookControl = this.headControllerContainer;
        this.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_IDLE)
                .build());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));


        this.goalSelector.addGoal(4, new GolemMeleeAttackGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide){
            this.setYRot(this.yBodyRot);
            var animSystem = this.getAnimationSystem();

            double x = this.getLookControl().getWantedX();
            double y = this.getLookControl().getWantedY();
            double z = this.getLookControl().getWantedZ();

            Vec3 deltaMovement = this.getDeltaMovement().multiply(1,0,1);
            double speed = deltaMovement.length();

            if (x == 0 && y == 0 && z == 0) {
                this.getLookControl().setLookAt(this.getEyePosition().add(this.getLookAngle()));
            }else if (this.getTarget() == null) {
                if (speed > 0.1) {
                    this.getLookControl().setLookAt(this.getEyePosition().add(this.getLookAngle()));
                }
            }



            var mainTicker = animSystem.getTicker(MAIN_LAYER);
            if (mainTicker != null) {
                var animation = mainTicker.getAnimation();
                if (speed > 0.01) {


                    if (this.isIdleAnim(animation) || animation.isToNullTransition()) {
                        animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_WALK)
                                .build());
                    }
                } else {
                    if (this.isWalkingAnim(animation) || animation.isToNullTransition()) {
                        animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_IDLE)
                                .build());
                    }

                }
            }else{
                animSystem.startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_IDLE)
                        .build());
            }


        }else{
            this.getHeadControllerContainer().clientTick();
        }
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {

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


    public static class GolemMeleeAttackGoal extends Goal {

        public int attackTime = 0;

        public GolemOfLastResort golem;

        public GolemMeleeAttackGoal(GolemOfLastResort golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            return this.golem.getTarget() != null;
        }

        @Override
        public void start() {
            attackTime = 0;
        }

        @Override
        public void tick() {
            var target = this.golem.getTarget();
            if (target != null){
                if (attackTime <= 0) {

                    var box = this.golem.getBoundingBox().inflate(1.25f);
                    var targetBox = target.getHitbox();


                    if (box.intersects(targetBox)) {
                        this.golem.getLookControl().setLookAt(target);
                        this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, target.position());
                        attackTime = 10;

                        this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.ANIMATION);
                        Animation animation = this.golem.random.nextBoolean() ? REAnimations.ILLAGER_GOLEM_STRIKE_1.get() : REAnimations.ILLAGER_GOLEM_STRIKE_2.get();
                        this.golem.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(animation)
                                        .important()
                                        .setSpeed(0.8f)
                                .build());

                    }else {

                        this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.LOOK);
                        this.golem.getLookControl().setLookAt(target);
                        var navigation = this.golem.getNavigation();
                        navigation.moveTo(target, 1.2f);
                    }

                }else{
                    this.golem.getNavigation().stop();
                    this.golem.getLookControl().setLookAt(this.golem.getEyePosition().add(this.golem.getLookAngle()));
                    attackTime--;
                    if (attackTime == 6){
                        Vec3 forward = this.golem.getForward();

                        Vec2 direction = new Vec2(
                                (float)forward.x,
                                (float)forward.z
                        );
                        var targets = FDTargetFinder.getEntitiesInArc(LivingEntity.class, this.golem.level(), this.golem.position().add(0,-1,0), direction, FDMathUtil.FPI, this.golem.getBbHeight() + 2,
                                this.golem.getBbWidth() + 1.5f);
                        for (var t : targets) {
                            if (t != this.golem) {
                                this.golem.doHurtTarget(t);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void stop() {
            this.golem.setTarget(null);
            this.golem.getNavigation().stop();
            this.golem.getLookControl().setLookAt(this.golem.getEyePosition().add(this.golem.getLookAngle()));
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.getTarget() != null;
        }

    }

}
