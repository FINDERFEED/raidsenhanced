package com.finderfeed.raids_enhanced;

import com.finderfeed.fdlib.network.FDPacketHandler;
import com.finderfeed.raids_enhanced.content.packets.REPosEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class REUtil {

    public static final int GOLEM_SMACK = 1;
    public static final int LIGHTNING_DEBRIS = 2;
    public static final int DRILL_DEATH = 3;

    public static void drillDeath(ServerLevel serverLevel, Vec3 pos, double radius){
        posEvent(serverLevel, pos, DRILL_DEATH, 0, radius);
    }

    public static void golemSmackParticles(ServerLevel serverLevel, Vec3 pos, double radius){
        posEvent(serverLevel, pos, GOLEM_SMACK, 0, radius);
    }

    public static void lightningDebris(ServerLevel serverLevel, Vec3 pos, double radius){
        posEvent(serverLevel, pos, LIGHTNING_DEBRIS, 0, radius);
    }

    public static void posEvent(ServerLevel serverLevel, Vec3 pos, int event, int data, double radius){
        FDPacketHandler.INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.x, pos.y, pos.z, radius, serverLevel.dimension())), new REPosEventPacket(pos, event, data));
    }

    public static Vec3 calculateMortarProjectileVelocity(Vec3 startPos, Vec3 endPos, double gravity, int tickTravelTime){

        Vec3 between = endPos.subtract(startPos);

        double horizontalDistance = Math.sqrt(between.x * between.x + between.z * between.z);

        double d = between.y;


        double horizontalSpeed = horizontalDistance / tickTravelTime;

        //d = vt + at^2 * 1/2
        //v = ((at^2 * 1/2) - d) / (-t)

        double verticalSpeed = ((gravity * tickTravelTime * tickTravelTime / 2) - d) / (-tickTravelTime);


        Vec3 result = between.multiply(1,0,1).normalize().multiply(horizontalSpeed,0,horizontalSpeed).add(0,verticalSpeed,0);

        return result;
    }

}
