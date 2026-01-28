package com.finderfeed.raids_enhanced.content.entities.raid_blimp.navigation;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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


    public RaidBlimpPathNavigation(Mob p_26424_, Level p_26425_) {
        super(p_26424_, p_26425_);
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }


        if (!this.isDone()) {
            this.processPath();

            if (nextPos != null) {
                this.mob.getMoveControl().setWantedPosition(nextPos.x, nextPos.y, nextPos.z, this.speedModifier);
            }

        }else{
            this.path = null;
            this.currentNode = 0;
            this.nextPos = null;
        }

    }


    private void processPath(){
        if (!this.isDone()){
            Vec3 ePos = this.mob.position();

            if (nextPos != null){
                if (ePos.distanceTo(nextPos) < 2){
                    if (nextMoveNode >= this.path.getNodeCount() - 1){
                        this.path.setNextNodeIndex(this.path.getNodeCount());
                    }else {
                        currentNode = nextMoveNode;
                        recalculationCooldown = 0;
                    }
                }
            }

            if (recalculationCooldown <= 0) {
                int nextMoveNode = currentNode;
                for (int i = currentNode + 1; i < this.path.getNodeCount(); i++) {

                    Vec3 v = this.path.getEntityPosAtNode(this.mob, i);
                    ClipContext clipContext = new ClipContext(v, ePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
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
        if (res = super.moveTo(p_26537_, p_26538_)) {
            this.currentNode = 0;
            this.nextPos = null;
            this.nextMoveNode = 0;
        }
        return res;
    }
}
