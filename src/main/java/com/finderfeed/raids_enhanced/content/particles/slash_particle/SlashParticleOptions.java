package com.finderfeed.raids_enhanced.content.particles.slash_particle;

import com.finderfeed.fdlib.util.FDByteBufCodecs;
import com.finderfeed.fdlib.util.FDCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class SlashParticleOptions implements ParticleOptions {

    public static StreamCodec<RegistryFriendlyByteBuf, SlashParticleOptions> streamCodec(ParticleType<?> type){
        return StreamCodec.composite(
                FDByteBufCodecs.VEC3, v->v.direction,
                ByteBufCodecs.INT, v->v.lifetime,
                ByteBufCodecs.FLOAT, v->v.pitch,
                ByteBufCodecs.FLOAT, v->v.size,
                ByteBufCodecs.BOOL, v->v.reversedSide,
                ((vec3, lifetime, aFloat, aFloat2, aBoolean) -> {
                    return new SlashParticleOptions(type, vec3, lifetime, aFloat, aFloat2, aBoolean);
                })
        );
    }

    public static MapCodec<SlashParticleOptions> codec(ParticleType<?> type){
        return RecordCodecBuilder.mapCodec(p->p.group(
                FDCodecs.VEC3.fieldOf("direction").forGetter(v->v.direction),
                Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime),
                Codec.FLOAT.fieldOf("pitch").forGetter((v->v.pitch)),
                Codec.FLOAT.fieldOf("size").forGetter((v->v.size)),
                Codec.BOOL.fieldOf("reversedSide").forGetter((v->v.reversedSide))
        ).apply(p, ((vec3, lifetime, aFloat, aFloat2, aBoolean) -> {
            return new SlashParticleOptions(type, vec3, lifetime, aFloat, aFloat2, aBoolean);
        })));
    }

    public ParticleType<?> particleType;
    public Vec3 direction;
    public int lifetime;
    public float pitch;
    public float size;
    public boolean reversedSide;

    public SlashParticleOptions(ParticleType<?> particleType, Vec3 direction, int lifetime, float pitch, float size, boolean reversedSide) {
        this.particleType = particleType;
        this.reversedSide = reversedSide;
        this.direction = direction;
        this.lifetime = lifetime;
        this.pitch = pitch;
        this.size = size;
    }

    @Override
    public ParticleType<?> getType() {
        return particleType;
    }

}
