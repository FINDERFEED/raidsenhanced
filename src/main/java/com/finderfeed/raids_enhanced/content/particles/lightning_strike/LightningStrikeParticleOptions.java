package com.finderfeed.raids_enhanced.content.particles.lightning_strike;

import com.finderfeed.fdlib.util.FDByteBufCodecs;
import com.finderfeed.fdlib.util.FDCodecs;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class LightningStrikeParticleOptions extends SimpleTexturedParticleOptions {

    public static MapCodec<LightningStrikeParticleOptions> lightningMapCodec(ParticleType<?> particleType){
        return RecordCodecBuilder.mapCodec(p->p.group(
                FDCodecs.VEC3.fieldOf("direction").forGetter(v->v.direction),
                Codec.FLOAT.fieldOf("size").forGetter(v->v.size),
                Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime)
        ).apply(p, (direction, size, lifetime) -> {
            return new LightningStrikeParticleOptions(particleType, direction, size, lifetime);
        }));
    }

    public static StreamCodec<FriendlyByteBuf, LightningStrikeParticleOptions> lightningStreamCodec(ParticleType<?> particleType){
        return StreamCodec.composite(
                FDByteBufCodecs.VEC3, v->v.direction,
                ByteBufCodecs.FLOAT, v->v.size,
                ByteBufCodecs.INT,v->v.lifetime,
                (direction, size, lifetime) -> {
                    return new LightningStrikeParticleOptions(particleType, direction, size, lifetime);
                }
        );
    }

    public Vec3 direction;

    public LightningStrikeParticleOptions(ParticleType<?> particleType, Vec3 direction, float size, int lifetime) {
        super(particleType, size, lifetime);
        this.direction = direction;
    }



}
