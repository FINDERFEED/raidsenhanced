package com.finderfeed.raids_enhanced.content.items;

import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.content.entities.ball_lightning.BallLightningEntity;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons.RaidBlimpCannonProjectile;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts.RaidBlimpPart;
import com.finderfeed.raids_enhanced.content.entities.vertical_lightning_strike.VerticalLightningStrikeAttack;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.slash_particle.SlashParticleOptions;
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

//            BallLightningEntity.summon(player, level, player.getEyePosition(), player.getLookAngle());

            VerticalLightningStrikeAttack.summon(player, player.position());

        }else{
//            Vec3 ppos = player.position().add(0,1,0).add(player.getLookAngle());
//            level.addParticle(new SlashParticleOptions(REParticles.ELECTRIC_SLASH.get(), player.getLookAngle(), 4, 0,3f,false), ppos.x,ppos.y,ppos.z,0,0,0);
        }

        return super.use(level, player, hand);
    }


}
