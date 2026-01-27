package com.finderfeed.raids_enhanced.content.entities.raid_blimp;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.BoneTransformationController;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

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

        var yxr1 = cannonr1.getCurrentYXRotation(v);
        cannonRight1.addXRot(yxr1.second);
        cannonRight1.addYRot(yxr1.first);


    }

}
