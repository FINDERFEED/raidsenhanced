package com.finderfeed.raids_enhanced.content.particles.vertical_lightning;

import com.finderfeed.raids_enhanced.content.particles.AnimatedSpriteParticle;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VerticalLightningParticle extends AnimatedSpriteParticle {

    public VerticalLightningParticle(SpriteSet spriteSet, SimpleTexturedParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(spriteSet, options, level, x, y, z, xd, yd, zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public void render(VertexConsumer vertex, Camera camera, float pticks) {
        Vec3 vec3 = camera.getPosition();
        float f = (float)(Mth.lerp((double)pticks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)pticks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)pticks, this.zo, this.z) - vec3.z());

        Quaternionf quaternionf = new Quaternionf(0, camera.rotation().y, 0, camera.rotation().w);

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(pticks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(pticks);
        vertex.vertex((double)avector3f[0].x(), (double)avector3f[0].y(), (double)avector3f[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        vertex.vertex((double)avector3f[1].x(), (double)avector3f[1].y(), (double)avector3f[1].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        vertex.vertex((double)avector3f[2].x(), (double)avector3f[2].y(), (double)avector3f[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        vertex.vertex((double)avector3f[3].x(), (double)avector3f[3].y(), (double)avector3f[3].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return LightTexture.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<SimpleTexturedParticleOptions> {

        private SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet){
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleTexturedParticleOptions p_107421_, ClientLevel p_107422_, double p_107423_, double p_107424_, double p_107425_, double p_107426_, double p_107427_, double p_107428_) {
            return new VerticalLightningParticle(spriteSet, p_107421_, p_107422_, p_107423_, p_107424_, p_107425_, p_107426_, p_107427_, p_107428_);
        }

    }

}
