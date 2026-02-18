package com.finderfeed.raids_enhanced.content.entities.ball_lightning;

import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.init.REConfigs;
import com.finderfeed.raids_enhanced.init.REEntities;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.UUID;

public class BallLightningEntity extends Entity {

    private UUID uuid;

    public static void summon(LivingEntity owner, Level level, Vec3 pos, Vec3 deltaMovement){
        BallLightningEntity ballLightningEntity = new BallLightningEntity(REEntities.BALL_LIGHTNING.get(), level);
        ballLightningEntity.setPos(pos);
        ballLightningEntity.uuid = owner.getUUID();
        ballLightningEntity.setDeltaMovement(deltaMovement);
        level.addFreshEntity(ballLightningEntity);
    }

    public BallLightningEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide){
            if (tickCount > 1000){
                this.remove(RemovalReason.DISCARDED);
            }
            var owner = this.getOwner();
            if (owner == null){
                this.remove(RemovalReason.DISCARDED);
            }
            this.detectAndExplode();
        }
        this.setPos(this.position().add(this.getDeltaMovement()));
    }

    private void detectAndExplode(){
        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());


        ClipContext clipContext = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        var result = level().clip(clipContext);
        var owner = this.getOwner();

        if (result.getType() != HitResult.Type.MISS){
            this.explode(owner, this.position());
        }else {
            var entities = FDHelpers.traceEntities(level(), start, end, 0.1f, (entity) -> {
                return entity instanceof LivingEntity livingEntity && entity != owner;
            });
            if (!entities.isEmpty()) {
                var entity = entities.get(random.nextInt(entities.size()));
                Vec3 bbCenter = entity.getBoundingBox().getCenter();
                Vec3 pos = new Vec3(
                        bbCenter.x,
                        Mth.clamp(this.position().y, bbCenter.y, bbCenter.y + entity.getBbHeight()),
                        bbCenter.z
                );
                this.explode(owner, pos);
            }
        }
    }

    private void explode(LivingEntity owner, Vec3 explodePos){
        if (level() instanceof ServerLevel serverLevel){
            serverLevel.sendParticles(new SimpleTexturedParticleOptions(REParticles.BALL_LIGHTNING_EXPLOSION.get(), 1f, 3), explodePos.x,explodePos.y,explodePos.z,1,0,0,0,0);
        }
        level().playSound(null, this.getX(), this.getY(), this.getZ(), RESounds.ENGINEER_BALL_LIGHTNING_EXPLOSION.get(), SoundSource.HOSTILE, 2f, random.nextFloat() * 0.2f + 0.9f);
        DamageSources damageSources = level().damageSources();
        DamageSource source;
        float damage;
        if (owner != null) {
            damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            source = damageSources.mobAttack(owner);
            if (owner instanceof Player player){
                damage = REConfigs.CONFIG.get().zapperStaffBallLightningDamage;
            }
        } else {

            damage = 5;
            source = damageSources.generic();
        }
        for (var entity : FDTargetFinder.getEntitiesInSphere(LivingEntity.class, level(),explodePos, 2f, e -> e != owner)) {
            entity.hurt(source, damage);
        }
        this.setRemoved(RemovalReason.DISCARDED);
    }

    private LivingEntity getOwner(){
        if (level() instanceof ServerLevel serverLevel && uuid != null && serverLevel.getEntity(uuid) instanceof LivingEntity livingEntity){
            return livingEntity;
        }
        return null;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("owner")){
            this.uuid = tag.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.uuid != null) {
            tag.putUUID("owner", this.uuid);
        }
    }

}
