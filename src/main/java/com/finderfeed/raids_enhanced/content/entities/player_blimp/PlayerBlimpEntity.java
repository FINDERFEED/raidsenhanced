package com.finderfeed.raids_enhanced.content.entities.player_blimp;

import com.google.common.collect.Lists;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerBlimpEntity extends VehicleEntity {

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

    public PlayerBlimpEntity(EntityType<? extends PlayerBlimpEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }



    @Override
    public void tick() {


        if (!level().isClientSide){
            if (this.isUnderWater()){
                this.ejectPassengers();
            }
        }

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
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
            }


            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95f));


        }else{
            this.setDeltaMovement(Vec3.ZERO);
            blimpRotationSpeed = 0;
        }

    }

    private void blimpMovement(){
        float groundFriction = this.getGroundFriction();
        if (groundFriction > 0){
            var movement = this.getDeltaMovement();
            this.setDeltaMovement(
                    movement.x * groundFriction,
                    movement.y,
                    movement.z * groundFriction
            );
        }
    }


    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            this.lerpSteps--;
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public void lerpTo(double p_38299_, double p_38300_, double p_38301_, float p_38302_, float p_38303_, int p_38304_) {
        this.lerpX = p_38299_;
        this.lerpY = p_38300_;
        this.lerpZ = p_38301_;
        this.lerpYRot = (double)p_38302_;
        this.lerpXRot = (double)p_38303_;
        this.lerpSteps = 10;
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? (float)this.lerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? (float)this.lerpYRot : this.getYRot();
    }

    private void controlBlimp(){
        if (this.isVehicle()) {
            boolean rotating = false;

            float rotspeedPerTick = 0.25f;
            float maxRotSpeed = 5;
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
                    return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
                } else {
                    return InteractionResult.SUCCESS;
                }
            }
        }else{
            return interactionresult;

        }
    }


    private boolean isUnderwater() {
        AABB aabb = this.getBoundingBox();
        double d0 = aabb.maxY + 0.001;
        int i = Mth.floor(aabb.minX);
        int j = Mth.ceil(aabb.maxX);
        int k = Mth.floor(aabb.maxY);
        int l = Mth.ceil(d0);
        int i1 = Mth.floor(aabb.minZ);
        int j1 = Mth.ceil(aabb.maxZ);
        boolean flag = false;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = i; k1 < j; k1++) {
            for (int l1 = k; l1 < l; l1++) {
                for (int i2 = i1; i2 < j1; i2++) {
                    blockpos$mutableblockpos.set(k1, l1, i2);
                    FluidState fluidstate = this.level().getFluidState(blockpos$mutableblockpos);
                    if (d0 < (double)((float)blockpos$mutableblockpos.getY() + fluidstate.getHeight(this.level(), blockpos$mutableblockpos))) {
                        if (!fluidstate.isSource()) {
                            return true;
                        }

                        flag = true;
                    }
                }
            }
        }

        return flag;
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

            for (Pose pose : p_38357_.getDismountPoses()) {
                for (Vec3 vec31 : list) {
                    if (DismountHelper.canDismountTo(this.level(), vec31, p_38357_, pose)) {
                        p_38357_.setPose(pose);
                        return vec31;
                    }
                }
            }
        }

        return super.getDismountLocationForPassenger(p_38357_);
    }

//    @Override
//    protected Vec3 getRiddenInput(Player player, Vec3 travelVec) {
//        return new Vec3(player.xxa,1,player.zza);
//    }
//
//    @Override
//    public void travel(Vec3 travelVector) {
//        if (!travelVector.equals(Vec3.ZERO)) {
//            super.travel(travelVector);
//        }
//    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player){
            return player;
        }
        return super.getControllingPassenger();
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
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

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

//    @Override
//    public HumanoidArm getMainArm() {
//        return HumanoidArm.RIGHT;
//    }

//    @Override
//    public Iterable<ItemStack> getArmorSlots() {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public ItemStack getItemBySlot(EquipmentSlot p_21127_) {
//        return ItemStack.EMPTY;
//    }
//
//    @Override
//    public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {
//
//    }


    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    protected Item getDropItem() {
        return Items.AIR;
    }
}
