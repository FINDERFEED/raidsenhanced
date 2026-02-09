package com.finderfeed.raids_enhanced.content.entities.vertical_lightning_strike;

import com.finderfeed.fdlib.util.rendering.renderers.QuadRenderer;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class VerticalLightningStrikeAttackRenderer extends EntityRenderer<VerticalLightningStrikeAttack> {

    public static final ResourceLocation PREPARATOR = RaidsEnhanced.location("textures/entities/lightning_preparator.png");

    public VerticalLightningStrikeAttackRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(VerticalLightningStrikeAttack attack, float pticks, float p_114487_, PoseStack matrices, MultiBufferSource src, int p_114490_) {
        int time = attack.tickCount;
        float p = (float) (time + pticks) / (VerticalLightningStrikeAttack.PREPARATION_TIME / 1.5f);
        if (p <= 1){
            int frame = Math.round(p * 4f);
            QuadRenderer.start(src.getBuffer(RenderType.text(PREPARATOR)))
                    .setAnimated(frame, 5)
                    .offsetOnDirection(0.01f)
                    .size(1f)
                    .pose(matrices)
                    .renderBack()
                    .render();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(VerticalLightningStrikeAttack p_114482_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
