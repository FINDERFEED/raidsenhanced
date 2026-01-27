package com.finderfeed.raids_enhanced.content.entities;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimatedObject;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.model_system.ModelSystem;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.model_system.entity_model_system.EntityModelSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class FDRaider extends Raider implements AnimatedObject {

    private EntityModelSystem<?> modelSystem = EntityModelSystem.create(this);

    public FDRaider(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
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
        super.addAdditionalSaveData(tag);
        this.modelSystem.saveAttachments(this.level().registryAccess(), tag);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.modelSystem.loadAttachments(this.level().registryAccess(), tag);
    }

}
