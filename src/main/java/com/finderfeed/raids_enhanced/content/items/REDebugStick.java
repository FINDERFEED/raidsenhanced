package com.finderfeed.raids_enhanced.content.items;

import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
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


            var blimps = FDTargetFinder.getEntitiesInSphere(RaidBlimp.class, player.level(), player.position(), 200);
            for (var bl : blimps){
                Vec3 lookPos = player.getLookAngle().multiply(1,0,1).normalize().scale(200);
                Vec3 pos = player.position().add(lookPos);
                bl.getNavigation().moveTo(pos.x, pos.y, pos.z, 1f);

            }

//            BallLightningEntity.summon(player, level, player.getEyePosition(), player.getLookAngle());

//            VerticalLightningStrikeAttack.summon(player, player.position());

//            RaidDrill raidDrill = new RaidDrill(REEntities.RAID_DRILL.get(), level);
//            raidDrill.testRaidPos = player.getOnPos();
//            raidDrill.setPos(player.position().add(player.getLookAngle().scale(15)));
//            level.addFreshEntity(raidDrill);

        }else{
//            Vec3 ppos = player.position().add(0,1,0).add(player.getLookAngle());
//            level.addParticle(new SlashParticleOptions(REParticles.ELECTRIC_SLASH.get(), player.getLookAngle(), 4, 0,3f,false), ppos.x,ppos.y,ppos.z,0,0,0);
        }

        return super.use(level, player, hand);
    }


}
