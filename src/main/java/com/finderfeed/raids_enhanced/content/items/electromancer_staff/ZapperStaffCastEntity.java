package com.finderfeed.raids_enhanced.content.items.electromancer_staff;

import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.content.entities.vertical_lightning_strike.VerticalLightningStrikeAttack;
import com.finderfeed.raids_enhanced.content.util.HorizontalCircleRandomDirections;
import com.finderfeed.raids_enhanced.init.REEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ZapperStaffCastEntity extends Entity {

    public UUID owner;
    public float ownerRotation;

    public static void summon(LivingEntity owner, Vec3 pos){
        ZapperStaffCastEntity staff = new ZapperStaffCastEntity(REEntities.ENGINEER_STAFF_CAST_ENTTITY.get(), owner.level());
        staff.setPos(pos);
        staff.owner = owner.getUUID();
        staff.ownerRotation = (float) Math.toRadians(-owner.getYRot() + 180);
        owner.level().addFreshEntity(staff);
    }

    public ZapperStaffCastEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide){
            if (owner == null){
                return;
            }

            if (((ServerLevel)level()).getEntity(owner) instanceof LivingEntity livingEntity){
                if (tickCount == 1){
                    this.spawnLightningAttacksAround(livingEntity,4, 4, ownerRotation);
                }else if (tickCount == 6){
                    this.spawnLightningAttacksAround(livingEntity,8, 8, ownerRotation + FDMathUtil.FPI / 2);
                }else if (tickCount == 11){
                    this.spawnLightningAttacksAround(livingEntity,12, 12, ownerRotation);
                }else if (tickCount == 16){
                    this.spawnLightningAttacksAround(livingEntity,16, 16, ownerRotation + FDMathUtil.FPI / 2);
                }
            }else{
                this.remove(RemovalReason.DISCARDED);
            }

            if (tickCount > 17){
                this.remove(RemovalReason.DISCARDED);
            }

        }
    }


    public void spawnLightningAttacksAround(LivingEntity owner, float distance, int lightningsCount, float angle){
        for (var dir : new HorizontalCircleRandomDirections(random, lightningsCount, 0f)){
            Vec3 realDir = dir.yRot(angle).scale(distance);
            BlockPos spawnPosCandidate = BlockPos.containing(this.position().add(realDir));
            this.trySpawnLightning(owner, spawnPosCandidate);
        }
    }

    private void trySpawnLightning(LivingEntity owner, BlockPos candidate){
        if (this.isPosValidForLightning(owner, candidate)){
            this.spawnLighting(owner, candidate);
        }else{
            for (int i = 1; i < 10; i++){
                if (this.isPosValidForLightning(owner, candidate.below(i))){
                    this.spawnLighting(owner, candidate.below(i));
                    return;
                }
            }

            for (int i = 1; i < 10; i++){
                if (this.isPosValidForLightning(owner, candidate.above(i))){
                    this.spawnLighting(owner, candidate.above(i));
                    return;
                }
            }
        }
    }

    private void spawnLighting(LivingEntity owner, BlockPos blockPos){
        Vec3 pos = blockPos.getCenter();
        VerticalLightningStrikeAttack.summon(owner, new Vec3(pos.x, Math.floor(pos.y), pos.z));
    }

    private boolean isPosValidForLightning(LivingEntity owner, BlockPos blockPos){
        BlockState state = level().getBlockState(blockPos);
        BlockState stateBelow = level().getBlockState(blockPos.below());
        return state.getCollisionShape(level(), blockPos).isEmpty() && !stateBelow.getCollisionShape(level(), blockPos.below()).isEmpty();
    }



    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("owner")){
            this.owner = tag.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (owner != null){
            tag.putUUID("owner", this.owner);
        }
    }

}
