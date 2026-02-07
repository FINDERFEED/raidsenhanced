package com.finderfeed.raids_enhanced.content.items;

import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons.RaidBlimpCannonProjectile;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts.RaidBlimpPart;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.init.REParticles;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class REDebugStick extends Item {

    private static Mob raidBlimp;

    public REDebugStick(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        if (!level.isClientSide){


        }else{
            Vec3 ppos = player.position().add(0,1,0).add(player.getLookAngle());
            level.addParticle(new LightningStrikeParticleOptions(REParticles.LIGHTNING_STRIKE.get(), player.getLookAngle(), 1f,4), ppos.x,ppos.y,ppos.z,0,0,0);
        }

        return super.use(level, player, hand);
    }


}
