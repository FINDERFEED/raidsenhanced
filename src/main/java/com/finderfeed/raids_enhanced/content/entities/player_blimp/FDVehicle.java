package com.finderfeed.raids_enhanced.content.entities.player_blimp;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimatedObject;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.model_system.ModelSystem;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.model_system.entity_model_system.EntityModelSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;

public abstract class FDVehicle extends VehicleEntity implements AnimatedObject {

    private EntityModelSystem<?> modelSystem = EntityModelSystem.create(this);

    public FDVehicle(EntityType<?> p_306130_, Level p_306167_) {
        super(p_306130_, p_306167_);

    }


    @Override
    public void tick() {
        super.tick();
        this.tickModelSystem();
    }

    public ModelSystem getModelSystem() {
        return this.modelSystem;
    }

    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.modelSystem.asServerside().syncToPlayer(player);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        this.modelSystem.saveAttachments(this.level().registryAccess(), tag);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        this.modelSystem.loadAttachments(this.level().registryAccess(), tag);
    }

}
