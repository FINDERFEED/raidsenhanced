package com.finderfeed.raids_enhanced.content.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SimpleTexturedParticleOptions implements ParticleOptions {

    public static MapCodec<SimpleTexturedParticleOptions> mapCodec(ParticleType<?> particleType){
        return RecordCodecBuilder.mapCodec(p->p.group(
                Codec.FLOAT.fieldOf("size").forGetter(v->v.size),
                Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime)
        ).apply(p, (size, lifetime) -> {
            return new SimpleTexturedParticleOptions(particleType, size, lifetime);
        }));
    }

    public static StreamCodec<FriendlyByteBuf, SimpleTexturedParticleOptions> streamCodec(ParticleType<?> particleType){
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, v->v.size,
                ByteBufCodecs.INT,v->v.lifetime,
                (size, lifetime) -> {
                    return new SimpleTexturedParticleOptions(particleType, size, lifetime);
                }
        );
    }

    public ParticleType<?> particleType;
    public float size;
    public int lifetime;

    public SimpleTexturedParticleOptions(ParticleType<?> particleType, float size, int lifetime) {
        this.particleType = particleType;
        this.size = size;
        this.lifetime = lifetime;
    }

    @Override
    public ParticleType<?> getType() {
        return particleType;
    }

}
