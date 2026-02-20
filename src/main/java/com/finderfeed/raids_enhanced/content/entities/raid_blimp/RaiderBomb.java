package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.FDEntity;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.raids_enhanced.content.entities.player_blimp.PlayerBlimpEntity;
import com.finderfeed.raids_enhanced.init.REConfigs;
import com.finderfeed.raids_enhanced.init.REEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.UUID;

public class RaiderBomb extends FDEntity {

    private UUID owner;

    public static void summon(LivingEntity raidBlimp, Vec3 pos, Vec3 startSpeed){
        RaiderBomb bomb = new RaiderBomb(REEntities.BOMB.get(), raidBlimp.level());
        bomb.owner = raidBlimp.getUUID();
        bomb.setPos(pos);
        bomb.setDeltaMovement(startSpeed);
        raidBlimp.level().addFreshEntity(bomb);
    }

    public RaiderBomb(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide){
            this.raycast();
            if (this.tickCount > 2000){
                this.explode(this.position());
            }
        }else {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + this.level().random.nextFloat() * 0.2 - 0.1,
                        this.getY() + this.getBbHeight() / 2 + this.level().random.nextFloat() * 0.2 - 0.1,
                        this.getZ() + this.level().random.nextFloat() * 0.2 - 0.1, 0, 0, 0
                );
                level().addParticle(ParticleTypes.FLAME,
                        this.getX() + this.level().random.nextFloat() * 0.2 - 0.1,
                        this.getY() + this.getBbHeight() / 2 + this.level().random.nextFloat() * 0.2 - 0.1,
                        this.getZ() + this.level().random.nextFloat() * 0.2 - 0.1, 0, 0, 0
                );
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().add(0, -this.getDefaultGravity(), 0));
        this.setPos(this.position().add(this.getDeltaMovement()));
    }

    public void raycast(){
        Vec3 deltaMovement = this.getDeltaMovement();
        Vec3 start = this.position().add(0,this.getBbHeight() / 2, 0);
        Vec3 end = start.add(deltaMovement);

        ClipContext clipContext = new ClipContext(start,end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        var res = level().clip(clipContext);

        if (res.getType() != HitResult.Type.MISS){
            this.explode(res.getLocation());
        }else {
            Entity owner = null;
            if (this.owner != null){
                owner = ((ServerLevel) level()).getEntity(this.owner);
            }
            var result = ProjectileUtil.getEntityHitResult(level(), owner, start, end, new AABB(start,end),(entity)->{
                return !(entity instanceof PlayerBlimpEntity);
            });
            if (result != null){
                this.explode(result.getLocation());
            }
        }
    }

    public void explode(Vec3 pos){
        Entity entity = null;
        if (this.owner != null){
            entity = ((ServerLevel) level()).getEntity(this.owner);
        }
        var entities = FDTargetFinder.getEntitiesInSphere(LivingEntity.class, level(), pos, 5);
        for (var e : entities){
            if (e == entity){
                continue;
            }
            DamageSource damageSource;
            float damage = 10;
            if (entity != null){
                damageSource = level().damageSources().mobAttack((LivingEntity) entity);
                if (!(entity instanceof Player player)) {
                    damage = (float) ((LivingEntity) entity).getAttributeValue(Attributes.ATTACK_DAMAGE);
                }else{
                    damage = REConfigs.CONFIG.get().handcannonBombDamage;
                }
            }else{
                damageSource = level().damageSources().generic();
            }
            e.hurt(damageSource, damage);
            Vec3 speed = e.position().subtract(this.position()).normalize().scale(0.5f).add(0,0.5,0);
            if (e instanceof ServerPlayer serverPlayer){
                FDLibCalls.setServerPlayerSpeed(serverPlayer, speed);
                serverPlayer.hasImpulse = true;
            }else{
                e.setDeltaMovement(speed);
                e.hasImpulse = true;
            }
        }

        level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f,1f);
        level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f,1.5f);
        for (var serverPlayer : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, level(), pos, 160)) {
            ((ServerLevel) level()).sendParticles(serverPlayer, ParticleTypes.EXPLOSION_EMITTER, true, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

        }
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null){
            tag.putUUID("owner", this.owner);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("owner")){
            this.owner = tag.getUUID("owner");
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    protected double getDefaultGravity() {
        return ServerPlayer.DEFAULT_BASE_GRAVITY;
    }

}
