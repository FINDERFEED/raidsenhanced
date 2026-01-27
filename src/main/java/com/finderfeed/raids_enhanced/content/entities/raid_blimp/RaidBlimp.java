package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REModels;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RaidBlimp extends FDRaider {

    private static FDModel clientModel;
    private static FDModel serverModel;

    public static final String BOMB_ILLAGER_LAYER = "ropes_bombs_its_yours_my_friend";

    private static final EntityDataAccessor<Integer> targetRight1 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetRight2 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetRight3 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetLeft1 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetLeft2 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> targetLeft3 = SynchedEntityData.defineId(RaidBlimp.class, EntityDataSerializers.INT);

    public RaidBlimpCannonsController cannonsController;

    public RaidBlimp(EntityType<? extends FDRaider> type, Level level) {
        super(type, level);
        this.cannonsController = new RaidBlimpCannonsController(this,
                targetRight1,targetRight2,targetRight3,
                targetLeft1,targetLeft2,targetLeft3
        );
        this.moveControl = new RaidBlimpMoveControl(this, 10, false);
        this.getAnimationSystem().startAnimation("IDLE", AnimationTicker.builder(REAnimations.RAID_AIRSHIP_FLY.get())
                .build());
        this.getAnimationSystem().startAnimation(BOMB_ILLAGER_LAYER, AnimationTicker.builder(REAnimations.RAID_AIRSHIP_ILLAGER_OBSERVE.get()).build());
        this.setNoGravity(true);

    }

    public static FDModel getModel(RaidBlimp raidBlimp){
        if (raidBlimp.level().isClientSide){
            if (clientModel == null){
                clientModel = new FDModel(REModels.RAID_BLIMP.get());
            }
            return clientModel;
        }else{
            if (serverModel == null){
                serverModel = new FDModel(REModels.RAID_BLIMP.get());
            }
            return serverModel;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            this.setYRot(this.yBodyRot);
        }

        this.cannonsController.tick();

    }

    @Override
    public boolean isNoGravity() {
        return true;
    }


    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    protected PathNavigation createNavigation(Level p_186262_) {
        RaidBlimpPathNavigation flyingpathnavigation = new RaidBlimpPathNavigation(this, p_186262_);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(targetRight1, -1);
        builder.define(targetRight2, -1);
        builder.define(targetRight3, -1);
        builder.define(targetLeft1 , -1);
        builder.define(targetLeft2 , -1);
        builder.define(targetLeft3 , -1);
    }

    @Override
    public void applyRaidBuffs(ServerLevel level, int p_37844_, boolean p_37845_) {

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    @Override
    public void push(double p_20286_, double p_20287_, double p_20288_) {

    }

    @Override
    public void push(Vec3 p_347665_) {

    }

    @Override
    public void push(Entity p_21294_) {

    }

    @Override
    protected void pushEntities() {

    }

    @Override
    public void knockback(double p_147241_, double p_147242_, double p_147243_) {

    }

    @Override
    protected void playBlockFallSound() {

    }

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {

    }

    @Override
    public boolean causeFallDamage(float p_147187_, float p_147188_, DamageSource p_147189_) {
        return false;
    }

}
