package com.finderfeed.raids_enhanced.content.items;

import com.finderfeed.fdlib.FDHelpers;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimpCannonProjectile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class REDebugStick extends Item {

    private static Mob raidBlimp;

    public REDebugStick(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        if (!level.isClientSide){

            var entities = FDHelpers.traceEntities(level, player.getEyePosition(),player.getEyePosition().add(player.getLookAngle().scale(5)), 0, (entity)->{
                return entity instanceof Mob ;
            });

            if (!entities.isEmpty()){
                var entity = entities.get(0);
                if (raidBlimp != null) {
                    if (entity != raidBlimp) {
                        raidBlimp = (Mob) entity;
                    }
                }else{
                    raidBlimp = (Mob) entity;
                }
            }else {
                if (raidBlimp != null) {
                    raidBlimp.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), 1f);
//                    RaidBlimpCannonProjectile.summon((RaidBlimp) raidBlimp,player.getEyePosition().add(player.getLookAngle().scale(2)), player.getLookAngle());
                }
            }


        }

        return super.use(level, player, hand);
    }


}
