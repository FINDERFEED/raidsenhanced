package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.BoneTransformationController;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.AxisAngle4d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RaidBlimpCannonBonesController implements BoneTransformationController<RaidBlimp> {

    //cannon_right/left_123
    public RaidBlimpCannonBonesController(){
    }

    @Override
    public void transformBone(RaidBlimp raidBlimp, FDModel fdModel, FDModelPart fdModelPart, PoseStack poseStack, MultiBufferSource multiBufferSource, String s, int i, int i1, float v) {

        FDModelPart cannonRight1 = fdModel.getModelPart("cannon_right_1");
        FDModelPart cannonRight2 = fdModel.getModelPart("cannon_right_2");
        FDModelPart cannonRight3 = fdModel.getModelPart("cannon_right_3");
        FDModelPart cannonLeft1  = fdModel.getModelPart("cannon_left_1");
        FDModelPart cannonLeft2  = fdModel.getModelPart("cannon_left_2");
        FDModelPart cannonLeft3  = fdModel.getModelPart("cannon_left_3");

        var cannonsController = raidBlimp.cannonsController;
        var cannonr1 = cannonsController.getCannonRight1();
        var cannonr2 = cannonsController.getCannonRight2();
        var cannonr3 = cannonsController.getCannonRight3();

        var cannonl1 = cannonsController.getCannonLeft1();
        var cannonl2 = cannonsController.getCannonLeft2();
        var cannonl3 = cannonsController.getCannonLeft3();

        var yxr1 = cannonr1.getCurrentYXRotation(v);
        var yxr2 = cannonr2.getCurrentYXRotation(v);
        var yxr3 = cannonr3.getCurrentYXRotation(v);

        var yxl1 = cannonl1.getCurrentYXRotation(v);
        var yxl2 = cannonl2.getCurrentYXRotation(v);
        var yxl3 = cannonl3.getCurrentYXRotation(v);

        var model = RaidBlimp.getModel(raidBlimp);
        model.resetTransformations();
        Vector3f yxr1e = this.getCannonEulerAngles(model, "cannon_right_1", yxr1.second, yxr1.first);
        Vector3f yxr2e = this.getCannonEulerAngles(model, "cannon_right_2", yxr2.second, yxr2.first);
        Vector3f yxr3e = this.getCannonEulerAngles(model, "cannon_right_3", yxr3.second, yxr3.first);
        Vector3f yxl1e = this.getCannonEulerAngles(model, "cannon_left_1", yxl1.second, yxl1.first);
        Vector3f yxl2e = this.getCannonEulerAngles(model, "cannon_left_2", yxl2.second, yxl2.first);
        Vector3f yxl3e = this.getCannonEulerAngles(model, "cannon_left_3", yxl3.second, yxl3.first);

        cannonRight1.addXRot((float) Math.toDegrees(yxr1e.x));
        cannonRight1.addYRot((float) Math.toDegrees(yxr1e.y));
        cannonRight1.addZRot((float) Math.toDegrees(yxr1e.z));

        cannonRight2.addXRot((float) Math.toDegrees(yxr2e.x));
        cannonRight2.addYRot((float) Math.toDegrees(yxr2e.y));
        cannonRight2.addZRot((float) Math.toDegrees(yxr2e.z));

        cannonRight3.addXRot((float) Math.toDegrees(yxr3e.x));
        cannonRight3.addYRot((float) Math.toDegrees(yxr3e.y));
        cannonRight3.addZRot((float) Math.toDegrees(yxr3e.z));


        cannonLeft1.addXRot((float) Math.toDegrees(yxl1e.x));
        cannonLeft1.addYRot((float) Math.toDegrees(yxl1e.y));
        cannonLeft1.addZRot((float) Math.toDegrees(yxl1e.z));

        cannonLeft2.addXRot((float) Math.toDegrees(yxl2e.x));
        cannonLeft2.addYRot((float) Math.toDegrees(yxl2e.y));
        cannonLeft2.addZRot((float) Math.toDegrees(yxl2e.z));

        cannonLeft3.addXRot((float) Math.toDegrees(yxl3e.x));
        cannonLeft3.addYRot((float) Math.toDegrees(yxl3e.y));
        cannonLeft3.addZRot((float) Math.toDegrees(yxl3e.z));

    }


    private Vector3f getCannonEulerAngles(FDModel model, String cannonName, float targetX, float targetY){
        Matrix4f transform = model.getModelPartTransformation( cannonName);
        transform.rotateZ((float) Math.toRadians(targetX));
        transform.rotateY((float) Math.toRadians(targetY));
        var rotation = transform.getNormalizedRotation(new Quaternionf());
        var eulers = rotation.getEulerAnglesXYZ(new Vector3f());
        return eulers;
    }

}
