package com.finderfeed.raids_enhanced.content.entities.raid_blimp.navigation;

import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class RaidBlimpPathNavigation extends FlyingPathNavigation {

    private int recalculationCooldown = 0;
    private int currentNode = 0;
    private int nextMoveNode = 0;
    private Vec3 nextPos = null;
    private RaidBlimp blimp;

    private boolean finishedMovingToPos = false;

    public RaidBlimpPathNavigation(RaidBlimp raidBlimp, Level p_26425_) {
        super(raidBlimp, p_26425_);
        this.blimp = raidBlimp;
    }

    @Override
    public void tick() {

        RaidBlimpMoveControl moveControl = (RaidBlimpMoveControl) this.blimp.getMoveControl();

        this.tick++;

        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {

            this.processPath();

            if (nextPos != null) {
                moveControl.setWantedPosition(nextPos.x, nextPos.y, nextPos.z, this.speedModifier);
            }

        }else{

            this.path = null;
            this.currentNode = 0;
            this.nextPos = null;
        }

    }

    public void setFinishedMovingToPos(boolean finishedMovingToPos) {
        this.finishedMovingToPos = finishedMovingToPos;
    }

    @Override
    public boolean isDone() {
        return super.isDone();
    }

    private void processPath(){
        if (!this.isDone()){
            Vec3 ePos = this.mob.position();

            if (nextPos != null){
                if (finishedMovingToPos){
                    if (nextMoveNode >= this.path.getNodeCount() - 1){
                        this.path.setNextNodeIndex(this.path.getNodeCount());
                        return;
                    }else {
                        finishedMovingToPos = false;
                        currentNode = nextMoveNode;
                        recalculationCooldown = 0;
                    }
                }
            }

            if (recalculationCooldown <= 0) {
                int nextMoveNode = currentNode;
                for (int i = currentNode + 1; i < this.path.getNodeCount(); i++) {

                    Vec3 v = this.path.getEntityPosAtNode(this.mob, i);
                    ClipContext clipContext = new ClipContext(v, ePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
                    var result = level.clip(clipContext);
                    if (result.getType() == HitResult.Type.MISS) {
                        if (i >= this.path.getNodeCount() - 1) {
                            nextMoveNode = this.path.getNodeCount() - 1;
                        }
                    } else {
                        nextMoveNode = i - 1;
                    }
                }


                this.nextMoveNode = nextMoveNode;
                this.nextPos = this.path.getEntityPosAtNode(this.mob, nextMoveNode);

                recalculationCooldown = 10;
            }


        }
        recalculationCooldown = Mth.clamp(recalculationCooldown - 1,0,Integer.MAX_VALUE);
    }

    @Override
    public boolean moveTo(@Nullable Path p_26537_, double p_26538_) {
        boolean res;
        Path p = this.path;
        if (res = super.moveTo(p_26537_, p_26538_) && p != p_26537_) {
            finishedMovingToPos = false;
            this.currentNode = 0;
            this.nextPos = null;
            this.nextMoveNode = 0;
        }
        return res;
    }
}
