package com.finderfeed.raids_enhanced.content.entities.electromancer;

import com.finderfeed.fdlib.init.FDEDataSerializers;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.client.particles.BoneAttachedParticles;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.content.entities.golem_of_last_resort.GolemOfLastResort;
import com.finderfeed.raids_enhanced.init.REModels;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ElectromancerEntity extends FDRaider {

    public static final String LIGHTNING_START = "lightning_start";

    public static final EntityDataAccessor<Byte> BYTE_PARTICLE_TRIGGER = SynchedEntityData.defineId(ElectromancerEntity.class, EntityDataSerializers.BYTE);

    public static final EntityDataAccessor<Boolean> LASER_ACTIVE = SynchedEntityData.defineId(ElectromancerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Vec3> LASER_TARGET = SynchedEntityData.defineId(ElectromancerEntity.class, FDEDataSerializers.VEC3.get());

    private Vec3 laserTargetOld;

    private static FDModel clientModel;
    private static FDModel serverModel;

    private BoneAttachedParticles boneAttachedParticles;

    public ElectromancerEntity(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
        if (level().isClientSide){
            this.boneAttachedParticles = new BoneAttachedParticles(getModel(level()), this, LIGHTNING_START, Vec3.ZERO);
        }
    }

    public static FDModel getModel(Level level){
        if (!level.isClientSide){
            if (serverModel == null){
                serverModel = new FDModel(REModels.ELECTROMANCER.get());
            }
            return serverModel;
        }else{
            if (clientModel == null){
                clientModel = new FDModel(REModels.ELECTROMANCER.get());
            }
            return clientModel;
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));


        this.goalSelector.addGoal(4, new LaserAttackGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        laserTargetOld = this.getLaserTarget();
        super.tick();
        if (!level().isClientSide){
            if (tickCount > 10){
                this.setLaserState(true);
            }
        }else{

        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor == LASER_TARGET){
            this.laserTargetOld = this.getLaserTarget();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LASER_TARGET, Vec3.ZERO);
        builder.define(BYTE_PARTICLE_TRIGGER, (byte)0);
        builder.define(LASER_ACTIVE, false);
    }

    public void setLaserState(boolean state){
        this.getEntityData().set(LASER_ACTIVE, state);
    }

    public void setLaserTarget(Vec3 pos){
        this.getEntityData().set(LASER_TARGET, pos);
    }

    public Vec3 getLaserTarget(){
        return this.getEntityData().get(LASER_TARGET);
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    public boolean isLaserActive() {
        return this.getEntityData().get(LASER_ACTIVE);
    }

    public static class LaserAttackGoal extends Goal {

        private ElectromancerEntity electromancerEntity;

        private int useTick = 0;

        public LaserAttackGoal(ElectromancerEntity electromancerEntity){
            this.electromancerEntity = electromancerEntity;
        }

        @Override
        public void start() {
            super.start();
            useTick = 0;
        }

        @Override
        public void tick() {
            super.tick();

        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return useTick < 100;
        }

    }

}
