package com.finderfeed.raids_enhanced.content.entities.ball_lightning;

import com.finderfeed.fdlib.util.rendering.renderers.QuadRenderer;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

public class BallLightningRenderer extends EntityRenderer<BallLightningEntity> {

    public static final ResourceLocation LOCATION = RaidsEnhanced.location("textures/entities/lightning_ball.png");

    public BallLightningRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(BallLightningEntity entity, float partialTicks, float p_114487_, PoseStack matrices, MultiBufferSource src, int p_114490_) {

        matrices.pushPose();

        Quaternionf quaternionf = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        matrices.mulPose(quaternionf);

        int frame;
        int time = entity.tickCount + entity.getId() / 2;

        float rotation = (entity.getId() % 4) * 90;


        int localTime = time % 10;
        if (localTime < 8){
            frame = localTime % 2;
        }else if (localTime == 8){
            frame = 2;
        }else{
            frame = 3;
        }

        matrices.mulPose(Axis.ZP.rotationDegrees(rotation));
        QuadRenderer.start(src.getBuffer(RenderType.text(LOCATION)))
                .renderBack()
                .pose(matrices)
                .size(1f)
                .setAnimated(frame, 4)
                .verticalRendering()
                .render();

        matrices.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(BallLightningEntity p_114482_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

}
