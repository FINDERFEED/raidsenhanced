package com.finderfeed.raids_enhanced.content.entities.engineer;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDFreeEntityRenderer;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.fdlib.util.rendering.renderers.QuadRenderer;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class EngineerRenderer implements FDFreeEntityRenderer<EngineerEntity> {

    public static final ResourceLocation ELECTRIC_RAY = RaidsEnhanced.location("textures/entities/electromancer_ray.png");
    public static final ResourceLocation ELECTRIC_BARRIER = RaidsEnhanced.location("textures/entities/electromancer_barrier.png");

    @Override
    public void render(EngineerEntity electromancerEntity, float v, float v1, PoseStack matrices, MultiBufferSource multiBufferSource, int i) {
        if (electromancerEntity.isLaserActive()) {

            Vec3 laserTarget = electromancerEntity.getLaserTarget(v);
            Matrix4f transformation = electromancerEntity.getModelPartTransformation(electromancerEntity, EngineerEntity.LIGHTNING_START, EngineerEntity.getModel(electromancerEntity.level()), v);
            Vec3 relativeLaserStart = new Vec3(transformation.transformPosition(new Vector3f()));
            Vec3 laserStart = relativeLaserStart.add(electromancerEntity.getPosition(v));;

            Vec3 between = laserTarget.subtract(laserStart);


            double len = between.length();


            Matrix4f mat = new Matrix4f();
            mat.translate((float)relativeLaserStart.x,(float)relativeLaserStart.y,(float)relativeLaserStart.z);
            FDRenderUtil.applyMovementMatrixRotations(mat,between);
            Vec3 n = FDMathUtil.getNormalVectorFromLineToPoint(laserStart,between.add(laserStart), Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());


            Matrix4f mt2 = new Matrix4f();
            FDRenderUtil.applyMovementMatrixRotations(mt2,between);
            Vector4f up = new Vector4f(0,0,1,1); mt2.transform(up);
            Vector4f left = new Vector4f(1,0,0,1); mt2.transform(left);
            Vec3 vup = new Vec3(up.x / up.w,up.y / up.w,up.z / up.w);
            Vec3 vleft = new Vec3(left.x,left.y,left.z);
            float angle = (float) FDMathUtil.angleBetweenVectors(n,vup);
            if (vleft.dot(n) > 0) {
                mat.rotateY(angle);
            }else{
                mat.rotateY(-angle);
            }


            matrices.pushPose();
            matrices.mulPose(mat);

            VertexConsumer vertex = multiBufferSource.getBuffer(RenderType.text(ELECTRIC_RAY));
            int laserFrame = electromancerEntity.tickCount % 4;

            float width = 1f;

            Matrix4f matr = matrices.last().pose();
            vertex.addVertex(matr, width/2,0,0).setColor(1f,1f,1f,1f).setUv(0,0.25f * (laserFrame + 1)).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(matrices.last(),0,0,1);
            vertex.addVertex(matr, width/2,(float) len,0).setColor(1f,1f,1f,1f).setUv((float)len / 2f,0.25f * (laserFrame + 1)).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(matrices.last(),0,0,1);
            vertex.addVertex(matr, -width/2,(float) len, 0).setColor(1f,1f,1f,1f).setUv((float)len / 2f,0.25f * laserFrame).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(matrices.last(),0,0,1);
            vertex.addVertex(matr, -width/2,0,0).setColor(1f,1f,1f,1f).setUv(0,0.25f * laserFrame).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(matrices.last(),0,0,1);

            matrices.popPose();


            matrices.pushPose();
            matrices.translate(0,electromancerEntity.getBbHeight() / 2,0);
            var rotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
            matrices.mulPose(rotation);
            QuadRenderer.start(vertex = multiBufferSource.getBuffer(RenderType.text(ELECTRIC_BARRIER)))
                    .renderBack()
                    .setAnimated(laserFrame,4)
                    .verticalRendering()
                    .pose(matrices)
                    .size(2)
                    .render();
            matrices.popPose();

        }
    }

}
