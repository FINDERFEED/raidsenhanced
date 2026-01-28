package com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts;

import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.rendering.FDEasings;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.init.REModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class RaidBlimpPartRenderer extends EntityRenderer<RaidBlimpPart> {

    private static HashMap<Integer, RaidBlimpPartDefinition> PARTS;

    public RaidBlimpPartRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(RaidBlimpPart part, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource src, int light) {

        if (PARTS == null){
            this.initParts();
        }

        int partType = part.getPartType();
        if (PARTS.containsKey(partType)) {
            var definition = PARTS.get(partType);
            var model = definition.fdModel;
            var texture = definition.texture;


            float rpt = part.landedTime >= 0 ? 0 : partialTicks;
            float rotation = part.rotation + rpt;

            Vec3 d = part.lastDeltaMovement;
            FDRenderUtil.applyMovementMatrixRotations(matrices, d);

            matrices.mulPose(Axis.XP.rotationDegrees(rotation * 100));



            float p = FDEasings.reversedEaseOut(Mth.clamp ((part.landedTime + Math.abs(rpt - partialTicks)) / 5f,0,1));

            if (part.landedTime >= 0) {
                matrices.mulPose(Axis.XP.rotationDegrees((float) Math.sin((part.landedTime + Math.abs(rpt - partialTicks)) * 5) * 20 * p));
            }

            model.render(matrices, src.getBuffer(RenderType.entityCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }

    }

    private void initParts(){
        PARTS = new HashMap<>();
        PARTS.put(1, new RaidBlimpPartDefinition(new FDModel(REModels.RAID_AIRSHIP_PART_1.get()), RaidsEnhanced.location("textures/entities/raid_airship_part_1.png")));
        PARTS.put(2, new RaidBlimpPartDefinition(new FDModel(REModels.RAID_AIRSHIP_PART_2.get()), RaidsEnhanced.location("textures/entities/raid_airship_part_2.png")));
    }

    @Override
    public ResourceLocation getTextureLocation(RaidBlimpPart part) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

}
