package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.util.FDTargetFinder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;

public class RaidBlimpCannonsController {

    private RaidBlimp raidBlimp;
    private RaidBlimpCannon cannonRight1;
    private RaidBlimpCannon cannonRight2;
    private RaidBlimpCannon cannonRight3;
    private RaidBlimpCannon cannonLeft1;
    private RaidBlimpCannon cannonLeft2;
    private RaidBlimpCannon cannonLeft3;

    public RaidBlimpCannonsController(RaidBlimp raidBlimp, EntityDataAccessor<Integer> targetRight1, EntityDataAccessor<Integer> targetRight2, EntityDataAccessor<Integer> targetRight3, EntityDataAccessor<Integer> targetLeft1, EntityDataAccessor<Integer> targetLeft2, EntityDataAccessor<Integer> targetLeft3) {
        this.raidBlimp = raidBlimp;
        this.cannonRight1 = new RaidBlimpCannon(this,"cannon_right_1", targetRight1);
        this.cannonRight2 = new RaidBlimpCannon(this,"cannon_right_2", targetRight2);
        this.cannonRight3 = new RaidBlimpCannon(this,"cannon_right_3", targetRight3);
        this.cannonLeft1 = new RaidBlimpCannon(this,"cannon_left_1", targetLeft1);
        this.cannonLeft2 = new RaidBlimpCannon(this,"cannon_left_2", targetLeft2);
        this.cannonLeft3 = new RaidBlimpCannon(this,"cannon_left_3", targetLeft3);
    }

    public void tick(){

        float cylinderSideHeight = 30;
        float cylinderRadius = 20;
        var targets = FDTargetFinder.getEntitiesInCylinder(Player.class, raidBlimp.level(), raidBlimp.position().add(0,-cylinderSideHeight,0), cylinderSideHeight * 2, cylinderRadius);

        cannonRight1.tick(targets);
        cannonRight2.tick(targets);
        cannonRight3.tick(targets);
        cannonLeft1.tick(targets);
        cannonLeft2.tick(targets);
        cannonLeft3.tick(targets);

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
