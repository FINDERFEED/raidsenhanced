package com.finderfeed.raids_enhanced.content.entities.falling_block;

import com.finderfeed.fdlib.nbt.AutoSerializable;
import com.finderfeed.fdlib.nbt.SerializableField;
import com.finderfeed.fdlib.util.FDProjectile;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.init.REEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class REFallingBlock extends FDProjectile implements AutoSerializable {

    public static final EntityDataAccessor<BlockState> STATE = SynchedEntityData.defineId(REFallingBlock.class, EntityDataSerializers.BLOCK_STATE);

    public static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.defineId(REFallingBlock.class, EntityDataSerializers.FLOAT);

    @SerializableField
    private int removingTicker = -1;

    public REFallingBlock(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public static REFallingBlock summon(Level level, BlockState state, Vec3 pos, Vec3 speed, float gravity){
        REFallingBlock block = new REFallingBlock(REEntities.FALLING_BLOCK.get(),level);
        block.setPos(pos);
        block.setDeltaMovement(speed);
        block.setBlockState(state);
        block.getEntityData().set(GRAVITY, gravity);
        level.addFreshEntity(block);
        return block;
    }
    public static REFallingBlock summon(Level level, BlockState state, Vec3 pos){
        return summon(level,state,pos,Vec3.ZERO);
    }

    public static REFallingBlock summon(Level level, BlockState state, Vec3 pos, Vec3 speed){
        return summon(level, state,pos,speed,0.025f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide){
            if (removingTicker != -1){
                removingTicker = Mth.clamp(removingTicker - 1,0, Integer.MAX_VALUE);
                if (removingTicker == 0){
                    this.remove(RemovalReason.DISCARDED);
                }
            }
        }

//        if (!level().isClientSide){
            this.applyGravity();
//        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide){
            if (removingTicker == -1) {

                var speed = Math.abs(this.getDeltaMovement().y);

                if (speed > 0.5) {
                    this.remove(RemovalReason.DISCARDED);
                } else {
                    int lifetime = (int) Math.floor(FDMathUtil.lerp(1,30,1 - speed / 0.5f));
                    removingTicker = lifetime;
                }
            }
        }
    }

    public BlockState getBlockState(){
        return this.entityData.get(STATE);
    }

    public void setBlockState(BlockState state){
        this.entityData.set(STATE,state);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        data
                .define(GRAVITY, 0.025f)
                .define(STATE, Blocks.STONE.defaultBlockState());
    }

    @Override
    public boolean save(CompoundTag tag) {
        tag.put("state", NbtUtils.writeBlockState(this.getBlockState()));
        tag.putFloat("gravity", this.getEntityData().get(GRAVITY));
        this.autoSave(tag);

        return super.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.setBlockState(NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK),tag.getCompound("state")));
        this.getEntityData().set(GRAVITY, tag.getFloat("gravity"));
        this.autoLoad(tag);
        super.load(tag);
    }


    @Override
    protected double getDefaultGravity() {
        return this.getEntityData().get(GRAVITY);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 120 * 120;
    }
}
