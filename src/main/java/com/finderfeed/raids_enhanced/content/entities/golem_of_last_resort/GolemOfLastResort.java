package com.finderfeed.raids_enhanced.content.entities.golem_of_last_resort;

import com.finderfeed.fdlib.nbt.AutoSerializable;
import com.finderfeed.fdlib.nbt.SerializableField;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.TransitionAnimation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.HeadControllerContainer;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.IHasHead;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.REUtil;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaiderBomb;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class GolemOfLastResort extends FDRaider implements IHasHead<GolemOfLastResort>, AutoSerializable {

    public static final String MAIN_LAYER = "IDLE";
    public static final String WALKING_LAYER = "WALKING";

    private static FDModel clientModel;
    private static FDModel serverModel;

    @SerializableField
    private int golemBombsCooldown = 0;

    private boolean isMeleeAttacking = false;
    private boolean isRangedAttacking = false;

    private boolean walkingWithHands = true;

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


        this.goalSelector.addGoal(3, new GolemBombsAttack(this));
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

            golemBombsCooldown = Mth.clamp(golemBombsCooldown - 1,0, Integer.MAX_VALUE);

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




        }else{
            this.getHeadControllerContainer().clientTick();
        }
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {

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

    public static class GolemBombsAttack extends Goal {

        public int attackTime;
        public GolemOfLastResort golem;


        public GolemBombsAttack(GolemOfLastResort golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            return this.golem.getTarget() != null && this.golem.golemBombsCooldown <= 0 && this.golem.random.nextFloat() < 0.05 && !this.golem.isMeleeAttacking
                    && (this.golem.getTarget().distanceTo(this.golem) > 5 || Math.abs(this.golem.getY() - this.golem.getTarget().getY()) > 2);
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

                List<Player> targets = FDTargetFinder.getEntitiesInCylinder(Player.class, golem.level(), golem.position().add(0,-20,0), 40,20);

                for (var tr : targets){
                    this.launchBombAtTarget(tr, pos);
                }

                if (!targets.contains(target)){
                    this.launchBombAtTarget(target, pos);
                }

            } else if (attackTime > 25){
                this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.LOOK);
                this.golem.golemBombsCooldown = 100;
                this.golem.isRangedAttacking = false;
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

        public GolemMeleeAttackGoal(GolemOfLastResort golem) {
            this.golem = golem;
        }

        @Override
        public boolean canUse() {
            return this.golem.getTarget() != null && !this.golem.isRangedAttacking;
        }

        @Override
        public void start() {
            attackTime = 0;
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
                    var box = this.golem.getBoundingBox().inflate(1.4f);
                    var targetBox = target.getHitbox();


                    if (box.intersects(targetBox)) {
                        this.golem.getLookControl().setLookAt(target);
                        this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, target.position());
                        this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.ANIMATION);

                        float rnd = this.golem.random.nextFloat();
                        if (rnd > 0.25){
                            this.meleeAttackType = 0;
                        }else if (rnd > 0.125){
                            this.meleeAttackType = 1;
                        }else{
                            this.meleeAttackType = 2;
                        }

//                        meleeAttackType = 2;
                        this.startAnimationAndAttackTicker(this.meleeAttackType);
                    }else {
                        this.golem.walkingWithHands = true;
                        this.golem.getHeadControllerContainer().setControllersMode(HeadControllerContainer.Mode.LOOK);
                        this.golem.getLookControl().setLookAt(target);
                        var navigation = this.golem.getNavigation();
                        navigation.moveTo(target, 1.2f);
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
            if (target != null){
                this.golem.getNavigation().moveTo(target, 0.8f);
            }
            if (attackTime > 10 && attackTime < 16){
                var targets = FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, this.golem.level(), this.golem.position().add(0,-1,0), this.golem.getBbHeight() + 2, this.golem.getBbWidth() + 1.5f);
                for (var t : targets) {
                    if (t != this.golem) {
                        this.golem.doHurtTarget(t);
                        if (t instanceof Player player && player.getUseItem().getItem() instanceof ShieldItem){
                            player.disableShield();
                        }
                    }
                }
            }else if (this.attackTime < 9){
            }
        }


        private void heavyAttack(){
            this.golem.walkingWithHands = false;
            this.golem.getLookControl().setLookAt(this.golem.getTarget());
            this.golem.getNavigation().stop();
            this.golem.lookAt(EntityAnchorArgument.Anchor.FEET, this.golem.getTarget().position());
            if (attackTime == 10){
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
                        if (t instanceof Player player && player.getUseItem().getItem() instanceof ShieldItem){
                            player.disableShield();
                        }
                    }
                }
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
                var targets = FDTargetFinder.getEntitiesInArc(LivingEntity.class, this.golem.level(), this.golem.position().add(0,-1,0), direction, FDMathUtil.FPI, this.golem.getBbHeight() + 2,
                        this.golem.getBbWidth() + 1.25f);
                for (var t : targets) {
                    if (t != this.golem) {
                        this.golem.doHurtTarget(t);
                    }
                }
            }
        }

        private void startAnimationAndAttackTicker(int attackType){
            if (attackType == 0){
                Animation animation = this.golem.random.nextBoolean() ? REAnimations.ILLAGER_GOLEM_STRIKE_1.get() : REAnimations.ILLAGER_GOLEM_STRIKE_2.get();
                this.golem.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(animation)
                        .important()
                        .setSpeed(1f)
                        .build());
                attackTime = 10;
            }else if (attackType == 1){
                this.golem.getAnimationSystem().startAnimation(MAIN_LAYER, AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_HEAVY_STRIKE)
                        .important()
                        .build());
                attackTime = 17;
            }else{
                this.golem.getAnimationSystem().startAnimation("WHIRLWIND", AnimationTicker.builder(REAnimations.ILLAGER_GOLEM_WHIRLWIND)
                                .setToNullTransitionTime(0)
                        .important()
                        .build());
                attackTime = 20;
            }
        }

        @Override
        public void stop() {
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
