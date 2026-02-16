package com.finderfeed.raids_enhanced.content.entities.vertical_lightning_strike;

import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.raids_enhanced.REClientUtil;
import com.finderfeed.raids_enhanced.REUtil;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.content.util.HorizontalCircleRandomDirections;
import com.finderfeed.raids_enhanced.init.REConfigs;
import com.finderfeed.raids_enhanced.init.REEntities;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class VerticalLightningStrikeAttack extends Entity {

    public static final int PREPARATION_TIME = 15;

    public UUID owner;

    public static void summon(LivingEntity owner, Vec3 pos){
        VerticalLightningStrikeAttack entity = new VerticalLightningStrikeAttack(REEntities.VERTICAL_LIGHTNING.get(), owner.level());
        entity.setPos(pos);
        entity.owner = owner.getUUID();
        owner.level().addFreshEntity(entity);
    }

    public VerticalLightningStrikeAttack(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide){
            if (tickCount >= PREPARATION_TIME + 1){
                this.damageEntities();
                ((ServerLevel)level()).playSound(null, this.getX(), this.getY(), this.getZ(), RESounds.LIGHTNING_STRIKE.get(), SoundSource.HOSTILE, 2f,random.nextFloat() * 0.2f + 0.8f);

            }else if (tickCount == PREPARATION_TIME){
                PositionedScreenShakePacket.send((ServerLevel) level(), FDShakeData.builder()
                        .frequency(5f)
                        .amplitude(2.5f)
                        .inTime(0)
                        .stayTime(0)
                        .outTime(6)
                        .build(),this.position(),10);
            }
        }else{
            if (tickCount == PREPARATION_TIME){
                REClientUtil.lightningDebris(this.position(), 0);
                this.level().addParticle(new SimpleTexturedParticleOptions(REParticles.VERTICAL_LIGHTNING.get(), 2f, 5), true, this.getX(), this.getY() + 2, this.getZ(), 0,0,0);
                for (var dir : new HorizontalCircleRandomDirections(level().random, 6, 0)){
                    Vec3 direction = dir.add(0,0.5,0);
                    Vec3 pos = this.position().add(direction.multiply(0.4,0.25,0.4));
                    level().addParticle(new LightningStrikeParticleOptions(REParticles.LIGHTNING_STRIKE.get(), direction, 1f, 4), true, pos.x, pos.y, pos.z, 0,0,0);
                }
            }
        }
    }

    private void damageEntities(){

        ServerLevel level = (ServerLevel) this.level();

        float damage = REConfigs.CONFIG.get().zapperStaffLightningDamage;
        Entity ownerEntity;

        if (this.owner != null) {
            ownerEntity = level.getEntity(owner);
            if (ownerEntity instanceof LivingEntity livingEntity && !(ownerEntity instanceof Player)){
                damage = (float) livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f;
            }
        } else {
            ownerEntity = null;
        }

        Vec3 cylinderStart = this.position().add(0,-1,0);
        for (var entity : FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, level, cylinderStart, 5,2f, e -> e != ownerEntity)) {
            if (ownerEntity instanceof LivingEntity livingEntity) {
                entity.hurt(this.level().damageSources().mobAttack(livingEntity), (float) damage);
            }else{
                entity.hurt(this.level().damageSources().generic(), (float) damage);
            }
        }
        this.remove(RemovalReason.DISCARDED);
    }



    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_326003_) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("owner")){
            this.owner = tag.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.owner != null){
            tag.putUUID("owner", owner);
        }
    }
}
