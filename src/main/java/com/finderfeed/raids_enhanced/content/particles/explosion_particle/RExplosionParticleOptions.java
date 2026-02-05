package com.finderfeed.raids_enhanced.content.particles.explosion_particle;

import com.finderfeed.raids_enhanced.init.REParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class RExplosionParticleOptions implements ParticleOptions {

    public static final MapCodec<RExplosionParticleOptions> CODEC = RecordCodecBuilder.mapCodec(p->p.group(
            Codec.FLOAT.fieldOf("size").forGetter(v->v.size),
            Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime)
    ).apply(p, RExplosionParticleOptions::new));

    public static final StreamCodec<FriendlyByteBuf, RExplosionParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, v->v.size,
            ByteBufCodecs.INT,v->v.lifetime,
            RExplosionParticleOptions::new
    );

    public float size;
    public int lifetime;

    public RExplosionParticleOptions(float size, int lifetime) {
        this.size = size;
        this.lifetime = lifetime;
    }

    @Override
    public ParticleType<?> getType() {
        return REParticles.EXPLOSION.get();
    }

}
