package com.finderfeed.raids_enhanced;

import net.minecraft.world.phys.Vec3;

public class REUtil {

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
