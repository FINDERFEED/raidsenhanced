package com.finderfeed.raids_enhanced.content.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatedSpriteParticle extends TextureSheetParticle {

    public SpriteSet spriteSet;

    public AnimatedSpriteParticle(SpriteSet spriteSet, SimpleTexturedParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
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

}
