package com.finderfeed.raids_enhanced.content.particles;

import com.finderfeed.fdlib.systems.stream_codecs.NetworkCodec;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

public class SimpleTexturedParticleOptions implements ParticleOptions {

    public static final Deserializer<SimpleTexturedParticleOptions> DESERIALIZER = new Deserializer<SimpleTexturedParticleOptions>() {
        @Override
        public SimpleTexturedParticleOptions fromCommand(ParticleType<SimpleTexturedParticleOptions> p_123733_, StringReader p_123734_) throws CommandSyntaxException {
            return new SimpleTexturedParticleOptions(REParticles.EXPLOSION.get(), 1f, 1);
        }

        @Override
        public SimpleTexturedParticleOptions fromNetwork(ParticleType<SimpleTexturedParticleOptions> p_123735_, FriendlyByteBuf p_123736_) {
            return STREAM_CODEC.fromNetwork(p_123736_);
        }
    };

    public static Codec<SimpleTexturedParticleOptions> CODEC = RecordCodecBuilder.create(p->p.group(
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particleType").forGetter(v->v.particleType),
            Codec.FLOAT.fieldOf("size").forGetter(v->v.size),
            Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime)
    ).apply(p, SimpleTexturedParticleOptions::new));

    public static NetworkCodec<SimpleTexturedParticleOptions> STREAM_CODEC = NetworkCodec.composite(
            NetworkCodec.registry(()-> ForgeRegistries.PARTICLE_TYPES), v->v.particleType,
            NetworkCodec.FLOAT, v->v.size,
            NetworkCodec.INT,v->v.lifetime,
            SimpleTexturedParticleOptions::new
    );

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

    @Override
    public void writeToNetwork(FriendlyByteBuf p_123732_) {
        STREAM_CODEC.toNetwork(p_123732_, this);
    }

    @Override
    public String writeToString() {
        return "";
    }

}
