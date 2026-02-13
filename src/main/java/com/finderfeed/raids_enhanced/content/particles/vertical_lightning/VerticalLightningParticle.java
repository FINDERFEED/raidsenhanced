package com.finderfeed.raids_enhanced.content.particles.vertical_lightning;

import com.finderfeed.raids_enhanced.content.particles.AnimatedSpriteParticle;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import org.jetbrains.annotations.Nullable;

public class VerticalLightningParticle extends AnimatedSpriteParticle {

    public VerticalLightningParticle(SpriteSet spriteSet, SimpleTexturedParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(spriteSet, options, level, x, y, z, xd, yd, zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_Y;
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
