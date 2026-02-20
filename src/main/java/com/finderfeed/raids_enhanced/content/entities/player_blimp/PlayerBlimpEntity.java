package com.finderfeed.raids_enhanced.content.entities.player_blimp;

import com.finderfeed.fdlib.network.FDPacketHandler;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REItems;
import com.google.common.collect.Lists;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerBlimpEntity extends FDVehicle {


    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(PlayerBlimpEntity.class, EntityDataSerializers.FLOAT);

    public static final String ROTATION_LAYER = "ROTATION";
    public static final String DANGLING_LIGHTS = "DANGLING_LIGHTS";


    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputForward;
    private boolean inputBackward;
    private boolean inputUp;
    private boolean inputDown;

    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    private float blimpRotationSpeed = 0;

    private float perpellerRotation;
    private float perpellerRotationO;
    private int perpellerStartTicks = 0;
    private double lastKnownSpeed;

    private int rotationDirection;

    private Vec3 oldPos;

    public PlayerBlimpEntity(EntityType<? extends PlayerBlimpEntity> type, Level level) {
        super(type, level);
        this.getAnimationSystem().startAnimation("PERPELLER", AnimationTicker.builder(REAnimations.PLAYER_BLIMP_PERPELLER)
                .build());
        this.getAnimationSystem().setAnimationsApplyListener(this::onAnimationsApplied);
    }

    private void onAnimationsApplied(FDModel model, Float pticks) {
        this.getAnimationSystem().setVariable("variable.propeller_rotation", FDMathUtil.lerp(perpellerRotationO, perpellerRotation, pticks));
    }


    @Override
    public void tick() {



        if (!level().isClientSide){
            if (this.isUnderWater()){
                this.ejectPassengers();
            }
        }


        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }



        super.tick();
        this.tickLerp();


        if (this.isControlledByLocalInstance()){
            this.blimpMovement();
            if (level().isClientSide) {
                this.controlBlimp();
                this.sendRotationToServer();
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95f));
        }else{
            this.setDeltaMovement(Vec3.ZERO);
            blimpRotationSpeed = 0;
        }

        if (oldPos == null){
            oldPos = this.position();
        }

        if (level().isClientSide){
            perpellerRotationO = perpellerRotation;
            if (this.getControllingPassenger() != null) {
                Vec3 delta = this.position().subtract(oldPos).multiply(1,0,1);
                double speed = Mth.clamp(delta.length(),0, 10);

                int startTicksTime = 20;
                if (speed > 0 && !(this.getGroundFriction() > 0)){
                    perpellerStartTicks = Mth.clamp(perpellerStartTicks + 1,0, startTicksTime);
                    lastKnownSpeed = speed;
                }else{
                    perpellerStartTicks = Mth.clamp(perpellerStartTicks - 1,0, startTicksTime);
                }
                float p = (float) perpellerStartTicks / startTicksTime;
                perpellerRotation += (float) lastKnownSpeed * 50 * p;

            }else{
                lastKnownSpeed = 0;
                perpellerStartTicks = Mth.clamp(perpellerStartTicks - 1,0,Integer.MAX_VALUE);
            }
        }else{

            if (tickCount % 4 == 0) {
                if (this.getGroundFriction() > 0) {
                    this.getAnimationSystem().stopAnimation("SOMEOTHERIDLE");
                } else {
                    this.getAnimationSystem().startAnimation("SOMEOTHERIDLE", AnimationTicker.builder(REAnimations.PLAYER_BLIMP_IDLE)
                                    .setToNullTransitionTime(10)
                            .build());
                }
            }

            if (this.getFirstPassenger() instanceof Player) {
                if (rotationDirection == 0) {
                    this.getAnimationSystem().stopAnimation(ROTATION_LAYER);
                    this.getAnimationSystem().stopAnimation(DANGLING_LIGHTS);
                } else if (rotationDirection == 1) {
                    this.getAnimationSystem().startAnimation(DANGLING_LIGHTS, AnimationTicker.builder(REAnimations.PLAYER_BLIMP_DANGLE_LIGHTS).build());
                    this.getAnimationSystem().startAnimation(ROTATION_LAYER, AnimationTicker.builder(REAnimations.PLAYER_BLIMP_TURN_LEFT).build());
                } else if (rotationDirection == -1) {
                    this.getAnimationSystem().startAnimation(DANGLING_LIGHTS, AnimationTicker.builder(REAnimations.PLAYER_BLIMP_DANGLE_LIGHTS).build());
                    this.getAnimationSystem().startAnimation(ROTATION_LAYER, AnimationTicker.builder(REAnimations.PLAYER_BLIMP_TURN_RIGHT).build());
                }
            }else{
                this.getAnimationSystem().stopAnimation(ROTATION_LAYER);
                this.getAnimationSystem().stopAnimation(DANGLING_LIGHTS);
            }
        }

        oldPos = this.position();

        this.collectEntitiesAround();

    }

    private void collectEntitiesAround(){
        List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            boolean flag = !this.level().isClientSide && !(this.getControllingPassenger() instanceof Player);

            for (Entity entity : list) {
                if (!entity.hasPassenger(this)) {
                    if (flag
                            && this.getPassengers().size() < this.getMaxPassengers()
                            && !entity.isPassenger()
                            && this.hasEnoughSpaceFor(entity)
                            && entity instanceof LivingEntity
                            && !(entity instanceof WaterAnimal)
                            && !(entity instanceof Player)) {
                        entity.startRiding(this);
                    } else {
                        this.push(entity);
                    }
                }
            }
        }
    }

    public boolean hasEnoughSpaceFor(Entity entity) {
        return entity.getBbWidth() < this.getBbWidth();
    }

    public boolean hurt(DamageSource p_38319_, float p_38320_) {
        if (this.isInvulnerableTo(p_38319_)) {
            return false;
        } else if (!this.level().isClientSide && !this.isRemoved()) {
            this.setDamage(this.getDamage() + p_38320_ * 20.0F);
            this.markHurt();
            this.gameEvent(GameEvent.ENTITY_DAMAGE, p_38319_.getEntity());
            boolean flag = p_38319_.getEntity() instanceof Player && ((Player)p_38319_.getEntity()).getAbilities().instabuild;
            if (flag || this.getDamage() > 40.0F) {
                if (!flag && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    this.destroy(p_38319_);
                }

                this.discard();
            }

            return true;
        } else {
            return true;
        }
    }

    public void setDamage(float p_38312_) {
        this.entityData.set(DAMAGE, p_38312_);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }


    protected void defineSynchedData() {;
        this.entityData.define(DAMAGE, 0.0F);
    }

    public void setRotatingState(int rotationDirection){
        this.rotationDirection = rotationDirection;
    }

    private void sendRotationToServer(){
        int rotationDirection = 0;
        if (inputLeft){
            rotationDirection = 1;
        }else if (inputRight){
            rotationDirection = -1;
        }
        FDPacketHandler.INSTANCE.sendToServer(new PlayerBlimpRotatingPacket(this.getId(), rotationDirection));
    }

    private void blimpMovement(){
        float groundFriction = this.getGroundFriction();
        if (groundFriction > 0){
            var movement = this.getDeltaMovement();
            this.setDeltaMovement(
                    movement.x * 0.1,
                    movement.y,
                    movement.z * 0.1
            );
        }
    }

    protected void positionRider(Entity entity, Entity.MoveFunction moveFunc) {
        if (this.hasPassenger(entity)) {
            float f = 0;
            float f1 = (float)((this.isRemoved() ? (double)0.01F : this.getPassengersRidingOffset()) + entity.getMyRidingOffset());
            if (this.getPassengers().size() > 1) {
                int i = this.getPassengers().indexOf(entity);
                if (i == 0) {
                    f = 0.2F;
                } else {
                    f = -0.6F;
                }

                if (entity instanceof Animal) {
                    f += 0.2F;
                }
            }

            Vec3 vec3 = (new Vec3((double)f, 0.0D, 0.0D)).yRot(-this.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
            moveFunc.accept(entity, this.getX() + vec3.x, this.getY() + (double)f1, this.getZ() + vec3.z);
        }
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            double d3 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
            this.setYRot(this.getYRot() + (float)d3 / (float)this.lerpSteps);
            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d0, d1, d2);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }

    public Vec2 getRotationVector() {
        return new Vec2(this.getXRot(), this.getYRot());
    }

    public Vec3 getForward() {
        return Vec3.directionFromRotation(this.getRotationVector());
    }


    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }


    @Override
    public void lerpTo(double p_38299_, double p_38300_, double p_38301_, float p_38302_, float p_38303_, int p_38304_, boolean p_38305_) {
        this.lerpX = p_38299_;
        this.lerpY = p_38300_;
        this.lerpZ = p_38301_;
        this.lerpYRot = (double)p_38302_;
        this.lerpXRot = (double)p_38303_;
        this.lerpSteps = 10;
    }

    private void controlBlimp(){
        if (this.isVehicle()) {
            boolean rotating = false;

            float rotspeedPerTick = 0.25f;
            float maxRotSpeed = 3;
            if (inputLeft) {
                blimpRotationSpeed -= rotspeedPerTick;
                rotating = true;
            }

            if (inputRight) {
                blimpRotationSpeed += rotspeedPerTick;
                rotating = true;
            }


            if (!rotating) {
                if (blimpRotationSpeed > 0) {
                    blimpRotationSpeed -= rotspeedPerTick;
                    if (blimpRotationSpeed < 0){
                        blimpRotationSpeed = 0;
                    }
                } else if (blimpRotationSpeed < 0) {
                    blimpRotationSpeed += rotspeedPerTick;
                    if (blimpRotationSpeed > 0){
                        blimpRotationSpeed = 0;
                    }
                }
            }

            blimpRotationSpeed = Mth.clamp(blimpRotationSpeed, -maxRotSpeed, maxRotSpeed);
            this.setYRot(this.getYRot() + this.blimpRotationSpeed);

            float speed = 0.0F;
            if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
                speed += 0.005F;
            }
            if (this.inputForward) {
                speed += 0.04F;
            }
            if (this.inputBackward) {
                speed -= 0.04F;
            }

            float speedVertical = 0;
            if (this.inputUp){
                speedVertical += 0.04f;
            }
            if (this.inputDown){
                speedVertical -= 0.04f;
            }



            this.setDeltaMovement(
                    this.getDeltaMovement()
                            .add((Mth.sin(-this.getYRot() * (float) (Math.PI / 180.0)) * speed),
                                    speedVertical,
                                    (Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * speed)
                            )
            );

        }
    }

    public float getGroundFriction() {
        AABB aabb = this.getBoundingBox();
        AABB aabb1 = new AABB(aabb.minX, aabb.minY - 0.001, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        int i = Mth.floor(aabb1.minX) - 1;
        int j = Mth.ceil(aabb1.maxX) + 1;
        int k = Mth.floor(aabb1.minY) - 1;
        int l = Mth.ceil(aabb1.maxY) + 1;
        int i1 = Mth.floor(aabb1.minZ) - 1;
        int j1 = Mth.ceil(aabb1.maxZ) + 1;
        VoxelShape voxelshape = Shapes.create(aabb1);
        float f = 0.0F;
        int k1 = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int l1 = i; l1 < j; l1++) {
            for (int i2 = i1; i2 < j1; i2++) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for (int k2 = k; k2 < l; k2++) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            blockpos$mutableblockpos.set(l1, k2, i2);
                            BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
                            if (!(blockstate.getBlock() instanceof WaterlilyBlock)
                                    && Shapes.joinIsNotEmpty(
                                    blockstate.getCollisionShape(this.level(), blockpos$mutableblockpos).move((double)l1, (double)k2, (double)i2),
                                    voxelshape,
                                    BooleanOp.AND
                            )) {
                                f += blockstate.getFriction(this.level(), blockpos$mutableblockpos, this);
                                k1++;
                            }
                        }
                    }
                }
            }
        }

        return f / (float)k1;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return true;
    }


    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult interactionresult = super.interact(player, hand);
        if (hand == InteractionHand.MAIN_HAND) {
            if (interactionresult != InteractionResult.PASS || this.isUnderWater()) {
                return interactionresult;
            } else if (player.isSecondaryUseActive()) {
                return InteractionResult.PASS;
            } else {
                if (!this.level().isClientSide) {
                    if (player.getVehicle() == this) {
                        player.stopRiding();
                        return InteractionResult.CONSUME;
                    }
                    if (player.startRiding(this)){
                        return InteractionResult.CONSUME;
                    } else{
                        return InteractionResult.PASS;
                    }
                } else {
                    return InteractionResult.SUCCESS;
                }
            }
        }else{
            return interactionresult;

        }
    }


    @Override
    public boolean canRiderInteract() {
        return true;
    }

    public void setInput(boolean left, boolean right, boolean forward, boolean backward, boolean shifting, boolean jumping) {
        this.inputLeft = left;
        this.inputRight = right;
        this.inputForward = forward;
        this.inputBackward = backward;
        this.inputUp = jumping;
        this.inputDown = shifting;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity p_38357_) {
        Vec3 vec3 = getCollisionHorizontalEscapeVector((double)(this.getBbWidth() * Mth.SQRT_OF_TWO), (double)p_38357_.getBbWidth(), p_38357_.getYRot());
        double d0 = this.getX() + vec3.x;
        double d1 = this.getZ() + vec3.z;
        BlockPos blockpos = BlockPos.containing(d0, this.getBoundingBox().maxY, d1);
        BlockPos blockpos1 = blockpos.below();
        if (!this.level().isWaterAt(blockpos1)) {
            List<Vec3> list = Lists.newArrayList();
            double d2 = this.level().getBlockFloorHeight(blockpos);
            if (DismountHelper.isBlockFloorValid(d2)) {
                list.add(new Vec3(d0, (double)blockpos.getY() + d2, d1));
            }

            double d3 = this.level().getBlockFloorHeight(blockpos1);
            if (DismountHelper.isBlockFloorValid(d3)) {
                list.add(new Vec3(d0, (double)blockpos1.getY() + d3, d1));
            }

            for(Pose pose : p_38357_.getDismountPoses()) {
                for(Vec3 vec31 : list) {
                    if (DismountHelper.canDismountTo(this.level(), vec31, p_38357_, pose)) {
                        p_38357_.setPose(pose);
                        return vec31;
                    }
                }
            }
        }

        return super.getDismountLocationForPassenger(p_38357_);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (this.isControlledByLocalInstance() && this.lerpSteps > 0) {
            this.lerpSteps = 0;
            this.absMoveTo(this.lerpX, this.lerpY, this.lerpZ, (float)this.lerpYRot, (float)this.lerpXRot);
        }
    }


    public double getPassengersRidingOffset() {
        return -0.1D;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity livingentity ? livingentity : super.getControllingPassenger();
    }

    @Override
    public boolean canCollideWith(Entity p_38376_) {
        return Boat.canVehicleCollide(this, p_38376_);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(
                this.getX() - 3,
                this.getY() - 1,
                this.getZ() - 3,
                this.getX() + 3,
                this.getY() + 6,
                this.getZ() + 3
        );
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis p_38335_, BlockUtil.FoundRectangle p_38336_) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(p_38335_, p_38336_));
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < this.getMaxPassengers();
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    protected double getDefaultGravity() {
        return 0;
    }

    protected void destroy(DamageSource p_219862_) {
        this.spawnAtLocation(this.getDropItem());
    }

    protected Item getDropItem() {
        return REItems.PLAYER_BLIMP.get();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }

    @Mod.EventBusSubscriber(modid = RaidsEnhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void hurtEvent(LivingHurtEvent event){
            var source = event.getSource();
            if (source != null && source.is(DamageTypes.FALL) && event.getEntity().getVehicle() instanceof PlayerBlimpEntity){
                event.setCanceled(true);
            }
        }

    }

}
