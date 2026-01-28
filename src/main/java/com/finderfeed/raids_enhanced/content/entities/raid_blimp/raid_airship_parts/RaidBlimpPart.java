package com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts;

import com.finderfeed.raids_enhanced.init.REEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class RaidBlimpPart extends Entity {

    public static final int WOODEN_STICK = 1;
    public static final int PROPELLER = 2;

    public static EntityDataAccessor<Integer> PART_TYPE = SynchedEntityData.defineId(RaidBlimpPart.class, EntityDataSerializers.INT);

    public int landedTime = -1;
    public int rotation = 0;
    public Vec3 lastDeltaMovement = Vec3.ZERO;

    public static void summon(Level level, Vec3 pos, Vec3 movement, int partType){
        RaidBlimpPart part = new RaidBlimpPart(REEntities.RAID_AIRSHIP_PART.get(), level);
        part.setPos(pos);
        part.setDeltaMovement(movement);
        part.setPartType(partType);
        part.lastDeltaMovement = movement;
        level.addFreshEntity(part);
    }

    public RaidBlimpPart(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide){
            if (tickCount++ > 400){
                this.setRemoved(RemovalReason.DISCARDED);
            }
        }else{
            if (!this.onGround()){
                for (int i = 0; i < 3; i++){
                    level().addParticle(ParticleTypes.SMOKE,true,
                            this.getX() + random.nextFloat() * 1  - 0.5,
                            this.getY() + random.nextFloat() * 1  - 0.5,
                            this.getZ() + random.nextFloat() * 1  - 0.5,
                            0,0,0
                    );
                    if (i == 1) {
                        level().addParticle(ParticleTypes.LARGE_SMOKE, true,
                                this.getX() + random.nextFloat() * 1 - 0.5,
                                this.getY() + random.nextFloat() * 1 - 0.5,
                                this.getZ() + random.nextFloat() * 1 - 0.5,
                                0, 0, 0
                        );
                    }
                }
            }
        }

        if (!this.getDeltaMovement().equals(Vec3.ZERO)){
            this.lastDeltaMovement = this.getDeltaMovement();
            if (tickCount > 5) {
                ClipContext clipContext = new ClipContext(this.position(), this.position().add(this.getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
                var res = level().clip(clipContext);
                if (res.getType() != HitResult.Type.MISS) {
                    Vec3 location = res.getLocation();
                    this.setOnGround(true);
                    this.teleportTo(location.x, location.y, location.z);
                    this.setDeltaMovement(Vec3.ZERO);
                }
            }
        }

        if (this.onGround()){
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
            landedTime++;
        }

        if (landedTime == -1) {
            rotation++;
        }

        this.applyGravity();
        this.setPos(this.position().add(this.getDeltaMovement()));
    }

    public void setPartType(int partType){
        this.getEntityData().set(PART_TYPE, partType);
    }

    public int getPartType(){
        return this.getEntityData().get(PART_TYPE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PART_TYPE, 1);
    }

    @Override
    protected double getDefaultGravity() {
        return ServerPlayer.DEFAULT_BASE_GRAVITY;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

}
