package com.finderfeed.raids_enhanced.content.entities.raid_blimp.navigation;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.phys.Vec3;

public class RaidBlimpMoveControl extends FlyingMoveControl {

    public RaidBlimpMoveControl(Mob mob, int turnMax, boolean hoversInPlace) {
        super(mob, turnMax, hoversInPlace);
    }

    @Override
    public void tick() {



        float speed = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));

        double d0 = this.wantedX - this.mob.getX();
        double d1 = this.wantedY - this.mob.getY();
        double d2 = this.wantedZ - this.mob.getZ();
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        if (d3 >= 2.5000003E-7F) {

            float f = (float)(Mth.atan2(d2, d0) * 180.0F / (float)Math.PI) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, speed * 10f));

            double d4 = Math.sqrt(d0 * d0 + d2 * d2);
            float f2 = (float)(-(Mth.atan2(d1, d4) * 180.0F / (float)Math.PI));
            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f2, (float)90));

        }


        Vec3 targetPos = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
        Vec3 between = targetPos.subtract(this.mob.position());
        float distance = (float) between.length();


        if (distance > 5) {
            speed = Mth.clamp(speed, 0, distance);
            Vec3 deltaMovement = this.mob.getLookAngle().normalize().scale(speed);
            this.mob.setDeltaMovement(deltaMovement);
        }else{
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(1,0.95,1));
        }

    }


}
