package com.finderfeed.raids_enhanced.content.items.electromancer_staff;

import com.finderfeed.fdlib.FDLibCalls;
import com.finderfeed.fdlib.systems.shake.FDShakeData;
import com.finderfeed.fdlib.systems.shake.PositionedScreenShakePacket;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.raids_enhanced.content.entities.ball_lightning.BallLightningEntity;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.slash_particle.SlashParticleOptions;
import com.finderfeed.raids_enhanced.content.util.HorizontalCircleRandomDirections;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class EngineerStaff extends Item {

    public EngineerStaff(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide){
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 lookAngle = player.getLookAngle();
            if (!this.usesAsLightning(player)){
                if (!player.isCreative()) {
                    player.getCooldowns().addCooldown(this, 40);
                }
                PositionedScreenShakePacket.send((ServerLevel)level, FDShakeData.builder()
                        .frequency(5f)
                        .amplitude(2.5f)
                        .inTime(0)
                        .stayTime(0)
                        .outTime(3)
                        .build(),player.position(),10);
                this.damageAndPushAwayEntities(player);
                this.castParticles(player);
                ElectromancerStaffCastEntity.summon(player, player.position());
                level.playSound(null, player.getX(),player.getY(), player.getZ(), RESounds.ENGINEER_LIGHTNING_CAST.get(), SoundSource.PLAYERS, 1f,1f);
            }else{
                Vec3 ppos = player.position().add(0,player.getEyeHeight() * 0.8f, 0).add(lookAngle.scale(0.5));
                serverLevel.sendParticles(new SlashParticleOptions(REParticles.ELECTRIC_SLASH.get(), lookAngle, 3,0f,2,level.random.nextBoolean()), ppos.x, ppos.y, ppos.z, 1,0,0,0,0);

                if (!player.isCreative()) {
                    player.getCooldowns().addCooldown(this, 10);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), RESounds.ENGINEER_BALL_LIGHTNING_LAUNCH.get(), SoundSource.HOSTILE, 2f, level.random.nextFloat() * 0.1f + 0.8f);
                BallLightningEntity.summon(player, level, player.getEyePosition().add(lookAngle), lookAngle.scale(2));
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));

        }
        return super.use(level, player, hand);
    }

    public boolean usesAsLightning(Player player){

        var lookAngle = player.getLookAngle();
        double dot = lookAngle.dot(new Vec3(0,1,0));

        if (player.onGround() && dot < -0.75){
            ClipContext clipContext = new ClipContext(player.getEyePosition(), player.getEyePosition().add(lookAngle.scale(3)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            var res = player.level().clip(clipContext);
            if (res.getType() != HitResult.Type.MISS){
                return false;
            }
        }

        return true;
    }

    private void damageAndPushAwayEntities(LivingEntity caster){
        var damage = 10;

        Vec3 cylinderStart = caster.position().add(0,-2,0);
        for (var entity : FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, caster.level(), cylinderStart, 3 + caster.getBbHeight(),3, e -> e != caster)){
            entity.hurt(caster.level().damageSources().mobAttack(caster), (float) (damage * 1.5f));
            Vec3 between = entity.position().subtract(caster.position());
            Vec3 pushVector = between.normalize().scale(2f);
            if (entity.onGround()){
                pushVector = pushVector.add(0,0.25,0);
            }
            if (entity instanceof ServerPlayer serverPlayer){
                FDLibCalls.setServerPlayerSpeed(serverPlayer, pushVector);
                serverPlayer.hasImpulse = true;
            }else{
                entity.setDeltaMovement(pushVector);
                entity.hasImpulse = true;
            }
        }

    }

    private void castParticles(LivingEntity caster){
        Vec3 lpos = caster.position().add(caster.getForward().multiply(1,0,1).normalize().scale(0.75f));
        for (var dir : new HorizontalCircleRandomDirections(caster.level().random, 6, 0)) {
            Vec3 direction = dir.add(0, 0.5, 0);
            Vec3 pos = lpos.add(direction.multiply(0.4, 0.25, 0.4));
            for (var serverPlayer : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, caster.level(), caster.position(), 40)) {

                ((ServerLevel)caster.level()).sendParticles(serverPlayer,
                        new LightningStrikeParticleOptions(REParticles.LIGHTNING_STRIKE.get(), direction, 1f, 4),
                        true, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }
        }
    }


}
