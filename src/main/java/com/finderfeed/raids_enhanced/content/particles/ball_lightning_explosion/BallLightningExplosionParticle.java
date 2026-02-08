package com.finderfeed.raids_enhanced.content.particles.ball_lightning_explosion;

import com.finderfeed.raids_enhanced.content.particles.AnimatedSpriteParticle;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import org.jetbrains.annotations.Nullable;

public class BallLightningExplosionParticle extends AnimatedSpriteParticle {

    public BallLightningExplosionParticle(SpriteSet spriteSet, SimpleTexturedParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(spriteSet, options, level, x, y, z, xd, yd, zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float p_107249_) {
        return LightTexture.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<SimpleTexturedParticleOptions>{

        private SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet){
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleTexturedParticleOptions p_107421_, ClientLevel p_107422_, double p_107423_, double p_107424_, double p_107425_, double p_107426_, double p_107427_, double p_107428_) {
            return new BallLightningExplosionParticle(spriteSet, p_107421_, p_107422_, p_107423_, p_107424_, p_107425_, p_107426_, p_107427_, p_107428_);
        }
    }

}
