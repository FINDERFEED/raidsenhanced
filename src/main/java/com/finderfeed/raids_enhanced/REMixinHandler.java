package com.finderfeed.raids_enhanced;

import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.init.REEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class REMixinHandler {

    public static void raidMixin(Raid raid, BlockPos raidPos, int currentGroup, int numGroups){

        if (numGroups == currentGroup) {
            Level level = raid.getLevel();
            int type = level.random.nextInt(3);
            if (type == 0){
                var golem = REEntities.GOLEM_OF_LAST_RESORT.get().create(level);
                if (golem != null) {
                    raid.joinRaid(currentGroup, golem, raidPos, false);
                }
            }else if (type == 1){
                var engineer = REEntities.ENGINEER.get().create(level);
                if (engineer != null) {
                    raid.joinRaid(currentGroup, engineer, raidPos, false);
                }
            }else if (type == 2) {
                var blimp = REEntities.RAID_BLIMP.get().create(level);
                if (blimp != null) {
                    int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, raidPos.getX(),raidPos.getZ());
                    Vec3 spawnPos = new Vec3(
                            raidPos.getX(),
                            height + RaidBlimp.HEIGHT_ABOVE_GROUND,
                            raidPos.getZ()
                    );
                    blimp.setPos(spawnPos);
                    blimp.setCurrentRaid(raid);
                    blimp.setWave(currentGroup);
                    blimp.setCanJoinRaid(true);
                    blimp.setTicksOutsideRaid(0);
                    level.addFreshEntity(blimp);
                    raid.addWaveMob(currentGroup, blimp, true);
                }
            }

        }

    }

}
