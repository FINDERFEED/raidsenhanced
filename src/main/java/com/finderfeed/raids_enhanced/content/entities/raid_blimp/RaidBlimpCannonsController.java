package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.util.FDTargetFinder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RaidBlimpCannonsController {

    private RaidBlimp raidBlimp;
    private RaidBlimpCannon cannonRight1;
    private RaidBlimpCannon cannonRight2;
    private RaidBlimpCannon cannonRight3;
    private RaidBlimpCannon cannonLeft1;
    private RaidBlimpCannon cannonLeft2;
    private RaidBlimpCannon cannonLeft3;

    private RaidBlimpCannon[] rightCannons;
    private RaidBlimpCannon[] leftCannons;

    public RaidBlimpCannonsController(RaidBlimp raidBlimp, EntityDataAccessor<Integer> targetRight1, EntityDataAccessor<Integer> targetRight2, EntityDataAccessor<Integer> targetRight3, EntityDataAccessor<Integer> targetLeft1, EntityDataAccessor<Integer> targetLeft2, EntityDataAccessor<Integer> targetLeft3) {
        this.raidBlimp = raidBlimp;
        this.cannonRight1 = new RaidBlimpCannon(this,"cannon_right_1", targetRight1, false);
        this.cannonRight2 = new RaidBlimpCannon(this,"cannon_right_2", targetRight2, false);
        this.cannonRight3 = new RaidBlimpCannon(this,"cannon_right_3", targetRight3, false);
        this.cannonLeft1 = new RaidBlimpCannon(this,"cannon_left_1", targetLeft1, true);
        this.cannonLeft2 = new RaidBlimpCannon(this,"cannon_left_2", targetLeft2, true);
        this.cannonLeft3 = new RaidBlimpCannon(this,"cannon_left_3", targetLeft3, true);

        this.rightCannons = new RaidBlimpCannon[]{
                cannonRight1,
                cannonRight2,
                cannonRight3
        };

        this.leftCannons = new RaidBlimpCannon[]{
                cannonLeft1,
                cannonLeft2,
                cannonLeft3
        };

    }

    public void tick(){

        float cylinderSideHeight = 30;
        float cylinderRadius = 40;
        var targets = FDTargetFinder.getEntitiesInCylinder(LivingEntity.class, raidBlimp.level(), raidBlimp.position().add(0,-cylinderSideHeight,0), cylinderSideHeight * 2, cylinderRadius, (target)->{
            return !target.isDeadOrDying() && target != this.getRaidBlimp();
        });

        cannonRight1.tick(targets);
        cannonRight2.tick(targets);
        cannonRight3.tick(targets);
        cannonLeft1.tick(targets);
        cannonLeft2.tick(targets);
        cannonLeft3.tick(targets);

        if (!this.getRaidBlimp().level().isClientSide) {
            this.processShooting(this.rightCannons);
            this.processShooting(this.leftCannons);
        }

    }

    private void processShooting(RaidBlimpCannon[] cannons) {
        int cooldown = 30;
        for (int i = 0; i < cannons.length; i++) {
            RaidBlimpCannon cannon = cannons[i];
            if (cannon.canShoot()) {
                cannon.shoot(cooldown);

                for (var c : cannons){
                    if (c != cannon && c.getTarget() == cannon.getTarget()){
                        c.setCooldown(cooldown);
                    }
                }

            }
        }
    }

    public boolean checkIfAlreadyHasTarget(RaidBlimpCannon checkCannon, LivingEntity target){

        for (var cannon : leftCannons){
            if (cannon == checkCannon) continue;
            if (cannon.getTarget() == target) return true;
        }

        for (var cannon : rightCannons){
            if (cannon == checkCannon) continue;
            if (cannon.getTarget() == target) return true;
        }

        return false;
    }

    public RaidBlimp getRaidBlimp() {
        return raidBlimp;
    }

    public RaidBlimpCannon getCannonLeft1() {
        return cannonLeft1;
    }

    public RaidBlimpCannon getCannonLeft2() {
        return cannonLeft2;
    }

    public RaidBlimpCannon getCannonLeft3() {
        return cannonLeft3;
    }

    public RaidBlimpCannon getCannonRight1() {
        return cannonRight1;
    }

    public RaidBlimpCannon getCannonRight2() {
        return cannonRight2;
    }

    public RaidBlimpCannon getCannonRight3() {
        return cannonRight3;
    }

}
