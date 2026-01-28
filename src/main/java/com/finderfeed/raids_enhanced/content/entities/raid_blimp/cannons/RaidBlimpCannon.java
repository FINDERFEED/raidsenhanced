package com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons;

import com.finderfeed.fdlib.data_structures.Pair;
import com.finderfeed.fdlib.util.FDTargetFinder;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class RaidBlimpCannon {

    private RaidBlimpCannonsController owner;

    private int shootCooldown = 20;

    private String bone;
    private EntityDataAccessor<Integer> clientTarget;
    private LivingEntity target;
    public boolean isLeft;
    public float yRot;
    public float xRot;
    public float yRotO;
    public float xRotO;

    public RaidBlimpCannon(RaidBlimpCannonsController controller, String bone, EntityDataAccessor<Integer> clientTarget, boolean isLeft){
        this.owner = controller;
        this.clientTarget = clientTarget;
        this.bone = bone;
        this.isLeft = isLeft;
    }

    public RaidBlimpCannonsController getOwner() {
        return owner;
    }

    public void tick(List<? extends LivingEntity> entitiesAround){
        var blimp = this.getOwner().getRaidBlimp();
        var level = blimp.level();
        if (!level.isClientSide) {
            this.processTargeting(entitiesAround);
            this.shootCooldown = Mth.clamp(this.shootCooldown - 1,0, Integer.MAX_VALUE);
        }else {
            this.processRotation();
        }
    }

    public void setCooldown(int cooldown){
        this.shootCooldown = cooldown;
    }

    public boolean canShoot(){
        return this.getTarget() != null && this.shootCooldown <= 0;
    }

    public void shoot(int cooldown){
        if (this.getTarget() != null){
            var posAndDir = this.getCannonPosAndDirection(1);
            Vec3 pos = posAndDir.first;
            Vec3 epos = this.getTargetPos(this.getTarget());
            this.sendCannonParticles(target, pos);
            Vec3 b = epos.subtract(pos);
            RaidBlimpCannonProjectile.summon(this.getOwner().getRaidBlimp(), pos, b);
            this.setCooldown(cooldown);
        }
    }

    private void sendCannonParticles(LivingEntity target, Vec3 pos){
        if (target.level() instanceof ServerLevel serverLevel){

            Vec3 targetPos = this.getTargetPos(target);
            Vec3 b = targetPos.subtract(pos).normalize();

            Vec3 particlePos = pos.add(b);

            for (var player : FDTargetFinder.getEntitiesInSphere(ServerPlayer.class, target.level(), pos, 120)){
                serverLevel.sendParticles(player, ParticleTypes.GUST, true, particlePos.x, particlePos.y, particlePos.z,1,0,0,0,0);
            }

            serverLevel.playSound(null, pos.x,pos.y,pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3f, 1.5f);

        }
    }

    private void processTargeting(List<? extends LivingEntity> entitiesAround){

        if (this.getTarget() != null){
            if (!this.isTargetValid(this.getTarget(), 0.1)){
                this.setTarget(null);
            }
        }

        if (this.getTarget() == null){
            var targets = this.getValidTargets(entitiesAround, 0.1);
            if (!targets.isEmpty()) {
                var target = targets.get(this.getOwner().getRaidBlimp().level().random.nextInt(targets.size()));
                this.setTarget(target);
            }
        }

    }

    private List<? extends LivingEntity> getValidTargets(List<? extends LivingEntity> entities, double dotRadius){
        List<LivingEntity> targets = new ArrayList<>();

        var cannonPosDir = this.getCannonPosAndDirection(1);
        Vec3 cannonPos = cannonPosDir.first;
        Vec3 cannonDir = cannonPosDir.second;

        for (var entity : entities){
            if (this.isTargetValid(cannonPos,cannonDir,entity,dotRadius) && !this.getOwner().checkIfAlreadyHasTarget(this, entity)){
                targets.add(entity);
            }
        }

        return targets;
    }

    private boolean isTargetValid(LivingEntity entity, double dotRadius){
        var cannonPosDir = this.getCannonPosAndDirection(1);
        return this.isTargetValid(cannonPosDir.first,cannonPosDir.second,entity, dotRadius);
    }

    private boolean isTargetValid(Vec3 cannonPos, Vec3 cannonDir, LivingEntity entity, double dotRadius){

        if (entity.isDeadOrDying() || entity.isRemoved()){
            return false;
        }

        Vec3 epos = this.getTargetPos(entity);

        if (epos.distanceTo(cannonPos) > 40){
            return false;
        }

        Vec3 pos = entity.position();
        Vec3 between = pos.subtract(cannonPos);
        double dot = between.normalize().dot(cannonDir.normalize());


        if (dot >= dotRadius){
            ClipContext clipContext = new ClipContext(cannonPos, epos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            var res = entity.level().clip(clipContext);
            if (res.getType() == HitResult.Type.MISS){
                return true;
            }
        }

        return false;
    }

    private void processRotation(){

        this.yRotO = yRot;
        this.xRotO = xRot;

        float rotationSpeed = 10f;

        var targetRotation = this.getTargetYXRotation();

        float yTargetRot = targetRotation.first;
        float xTargetRot = targetRotation.second;

        Vec3 targetRot = new Vec3(xTargetRot, yTargetRot, 0);
        Vec3 currentRot = new Vec3(xRot, yRot, 0);
        Vec3 between = targetRot.subtract(currentRot);
        float dist = (float) between.length();
        float shift = Mth.clamp(dist, 0, rotationSpeed);

        Vec3 shiftVector = between.normalize().scale(shift);
        this.yRot += (float) shiftVector.y;
        this.xRot += (float) shiftVector.x;

    }

    public Pair<Float, Float> getCurrentYXRotation(float pticks){
        return new Pair<>(
                FDMathUtil.lerp(this.yRotO, this.yRot, pticks),
                FDMathUtil.lerp(this.xRotO, this.xRot, pticks)
        );
    }

    private Pair<Float, Float> getTargetYXRotation(){

        var target = this.getTarget();
        if (target == null){
            return new Pair<>(0f,0f);
        }else{
            var cannonPosAndDir = this.getCannonPosAndDirection(1);
            var pos = cannonPosAndDir.first;
            var dir = cannonPosAndDir.second;
            var left = dir.cross(new Vec3(0,1,0));

            var targetPos = this.getTargetPos(target);

            Vec3 between = targetPos.subtract(pos);
            Vec3 nb = between.normalize();

            double sideCheck = left.dot(nb);
            double upOrDown = nb.dot(new Vec3(0,1,0));


            Vec3 nbxz = new Vec3(Math.sqrt(nb.x * nb.x + nb.z * nb.z), nb.y, 0);
            Vec3 dirxz = new Vec3(Math.sqrt(dir.x * dir.x + dir.z * dir.z), dir.y, 0);

            double verticalAngle = FDMathUtil.angleBetweenVectors(nbxz, dirxz);
            double horizontalAngle = FDMathUtil.angleBetweenVectors(nb.multiply(1,0,1), dir.multiply(1,0,1));

            if (sideCheck > 0){
                horizontalAngle = -horizontalAngle;
            }
            if ((!isLeft && upOrDown < 0) || (isLeft && upOrDown > 0)){
                verticalAngle = -verticalAngle;
            }

            return new Pair<>((float) Math.toDegrees(horizontalAngle), (float) Math.toDegrees(verticalAngle));
        }
    }

    private Pair<Vec3, Vec3> getCannonPosAndDirection(float pticks){
        RaidBlimp blimp = this.getOwner().getRaidBlimp();
        Matrix4f transform = blimp.getModelPartTransformation(blimp, bone, RaidBlimp.getModel(blimp));
        Vector3f cannonPosF = transform.transformPosition(new Vector3f());
        Vector3f cannonDirectionF = transform.transformDirection(new Vector3f(1,0,0));
        Vec3 cannonPos = new Vec3(cannonPosF.x, cannonPosF.y, cannonPosF.z).add(this.getOwner().getRaidBlimp().getPosition(pticks));
        Vec3 cannonDir = new Vec3(cannonDirectionF.x, cannonDirectionF.y, cannonDirectionF.z);
        if (this.isLeft){
            cannonDir = cannonDir.reverse();
        }
        return new Pair<>(cannonPos, cannonDir);
    }

    public void setTarget(LivingEntity target){
        if (target != null){
            shootCooldown = 10;
            this.target = target;
            this.getOwner().getRaidBlimp().getEntityData().set(clientTarget, target.getId());
        }else{
            this.target = null;
            this.getOwner().getRaidBlimp().getEntityData().set(clientTarget, -1);
        }
    }

    private Vec3 getTargetPos(LivingEntity livingEntity){
        return livingEntity.position().add(0,livingEntity.getBbHeight() / 2, 0);
    }

    public LivingEntity getTarget() {
        var level = owner.getRaidBlimp().level();
        if (!level.isClientSide) {
            return target;
        }else{
            if (level.getEntity(owner.getRaidBlimp().getEntityData().get(clientTarget)) instanceof LivingEntity livingEntity){
                return livingEntity;
            }
        }
        return null;
    }

}
