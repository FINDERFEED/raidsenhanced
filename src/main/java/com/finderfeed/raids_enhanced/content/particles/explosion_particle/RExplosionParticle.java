package com.finderfeed.raids_enhanced.content.particles.explosion_particle;

import com.finderfeed.fdlib.util.math.FDMathUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import org.jetbrains.annotations.Nullable;

public class RExplosionParticle extends TextureSheetParticle {

    private SpriteSet spriteSet;

    public RExplosionParticle(SpriteSet spriteSet, RExplosionParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        this.x = x;
        this.y = y;
        this.z = z;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = options.size;
        this.lifetime = options.lifetime;
        this.setSpriteFromAge(spriteSet);
        this.spriteSet = spriteSet;
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

    @Override
    protected int getLightColor(float p_107249_) {
        return LightTexture.FULL_BRIGHT;
    }

    public static class Factory implements ParticleProvider<RExplosionParticleOptions>{

        private SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet){
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(RExplosionParticleOptions options, ClientLevel p_107422_, double p_107423_, double p_107424_, double p_107425_, double p_107426_, double p_107427_, double p_107428_) {
            RExplosionParticle rExplosionParticle = new RExplosionParticle(spriteSet, options,p_107422_,p_107423_,p_107424_,p_107425_,p_107426_,p_107427_,p_107428_);
            return rExplosionParticle;
        }
    }

}
