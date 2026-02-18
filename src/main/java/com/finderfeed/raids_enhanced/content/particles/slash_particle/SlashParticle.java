package com.finderfeed.raids_enhanced.content.particles.slash_particle;

import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class SlashParticle extends TextureSheetParticle {

    public SlashParticleOptions options;
    public SpriteSet spriteSet;

    public SlashParticle(SpriteSet spriteSet, SlashParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.x = x;
        this.y = y;
        this.z = z;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.options = options;
        this.quadSize = options.size;
        this.lifetime = options.lifetime;
        this.setSpriteFromAge(spriteSet);
        this.spriteSet = spriteSet;
    }

    @Override
    public void render(VertexConsumer vertex, Camera camera, float pticks) {

        Matrix4f mat = new Matrix4f();

        Vec3 thisPos = new Vec3(
                FDMathUtil.lerp(xo, x, pticks),
                FDMathUtil.lerp(yo, y, pticks),
                FDMathUtil.lerp(zo, z, pticks)
        );

        Vec3 offset = thisPos.subtract(camera.getPosition());

        mat.translate(
                (float)offset.x,
                (float)offset.y,
                (float)offset.z
        );
        FDRenderUtil.applyMovementMatrixRotations(mat, options.direction);
        mat.rotateY(options.pitch);

        float u1 = this.getU0();
        float u2 = this.getU1();

        float v1 = this.getV0();
        float v2 = this.getV1();

        if (options.reversedSide) {
            u1 = this.getU1();
            u2 = this.getU0();
        }

        float w = options.size;
        vertex.vertex(mat, -w/2,-w/2,0).uv(u1,v1).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, -w/2,w/2,0).uv(u1,v2).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, w/2,w/2, 0).uv(u2,v2).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, w/2, -w/2, 0).uv(u2,v1).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, w/2, -w/2, 0).uv(u2,v1).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, w/2,w/2, 0).uv(u2,v2).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, -w/2,w/2,0).uv(u1,v2).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();
        vertex.vertex(mat, -w/2,-w/2,0).uv(u1,v1).color(1f, 1f, 1f, 1f).uv2(LightTexture.FULL_BRIGHT).endVertex();

    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Provider implements ParticleProvider<SlashParticleOptions> {

        private SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet){
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SlashParticleOptions p_107421_, ClientLevel p_107422_, double p_107423_, double p_107424_, double p_107425_, double p_107426_, double p_107427_, double p_107428_) {
            SlashParticle slashParticle = new SlashParticle(spriteSet, p_107421_, p_107422_, p_107423_, p_107424_, p_107425_, p_107426_, p_107427_, p_107428_);
            return slashParticle;
        }
    }

}
