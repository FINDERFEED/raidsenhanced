package com.finderfeed.raids_enhanced.content.items.handcannon;

import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaiderBomb;
import com.finderfeed.raids_enhanced.content.items.ItemWithDescription;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.init.REConfigs;
import com.finderfeed.raids_enhanced.init.REItems;
import com.finderfeed.raids_enhanced.init.REParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class HandCannon extends ItemWithDescription {

    public HandCannon(Properties p_41383_) {
        super(p_41383_, Component.translatable("raidsenhanced.item_description.handcannon").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide){

            if (!player.isCreative()) {
                player.getCooldowns().addCooldown(REItems.HANDCANNON.get(), REConfigs.CONFIG.get().handcannonUseCooldown);
            }

            Vec3 particlePos = player.position().add(0, player.getEyeHeight(),0);

            Vec3 lookAngle = player.getLookAngle();

            Vec3 left = lookAngle.cross(new Vec3(0,1,0)).normalize();
            Vec3 lup = left.cross(lookAngle).normalize();

            if (hand == InteractionHand.MAIN_HAND){
                particlePos = particlePos.add(lookAngle.scale(1f))
                        .add(left.scale(0.2f))
                        .add(lup.scale(-0.2f));
            }else{
                particlePos = particlePos.add(lookAngle.scale(1f))
                        .add(left.scale(-0.2f))
                        .add(lup.scale(-0.2f));
            }

            Vec3 bombSpawnPos = player.getEyePosition().add(lookAngle.scale(0.5));
            RaiderBomb.summon(player, bombSpawnPos, lookAngle.scale(2f));

            ((ServerLevel)level).sendParticles(new SimpleTexturedParticleOptions(REParticles.EXPLOSION.get(), 0.5f, 4), particlePos.x,particlePos.y,particlePos.z,1,0,0,0,0);
            ((ServerLevel)level).playSound(null, bombSpawnPos.x, bombSpawnPos.y, bombSpawnPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f, 1.5f);
            ((ServerLevel)level).playSound(null, bombSpawnPos.x, bombSpawnPos.y, bombSpawnPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f, 0.75f);

        }
        return super.use(level, player, hand);
    }

}
