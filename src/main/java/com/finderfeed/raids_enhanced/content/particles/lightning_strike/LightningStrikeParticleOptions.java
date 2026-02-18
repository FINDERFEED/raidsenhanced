package com.finderfeed.raids_enhanced.content.particles.lightning_strike;

import com.finderfeed.fdlib.systems.stream_codecs.NetworkCodec;
import com.finderfeed.fdlib.util.FDCodecs;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class LightningStrikeParticleOptions extends SimpleTexturedParticleOptions {

    public static final Deserializer<LightningStrikeParticleOptions> LIGHTNING_DESERIALIZER = new Deserializer<LightningStrikeParticleOptions>() {
        @Override
        public LightningStrikeParticleOptions fromCommand(ParticleType<LightningStrikeParticleOptions> p_123733_, StringReader p_123734_) throws CommandSyntaxException {
            return new LightningStrikeParticleOptions(REParticles.LIGHTNING_STRIKE.get(), new Vec3(0,1,0), 1f, 1);
        }

        @Override
        public LightningStrikeParticleOptions fromNetwork(ParticleType<LightningStrikeParticleOptions> p_123735_, FriendlyByteBuf p_123736_) {
            return LIGHTNING_STREAM_CODEC.fromNetwork(p_123736_);
        }
    };

    public static Codec<LightningStrikeParticleOptions> LIGHTNING_CODEC = RecordCodecBuilder.create(p->p.group(
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particleType").forGetter(v->v.particleType),
            FDCodecs.VEC3.fieldOf("direction").forGetter(v->v.direction),
            Codec.FLOAT.fieldOf("size").forGetter(v->v.size),
            Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime)
    ).apply(p, LightningStrikeParticleOptions::new));

    public static NetworkCodec<LightningStrikeParticleOptions> LIGHTNING_STREAM_CODEC = NetworkCodec.composite(
            NetworkCodec.registry(()-> ForgeRegistries.PARTICLE_TYPES), v->v.particleType,
            NetworkCodec.VEC3, v->v.direction,
            NetworkCodec.FLOAT, v->v.size,
            NetworkCodec.INT,v->v.lifetime,
            LightningStrikeParticleOptions::new
    );


    public Vec3 direction;

    public LightningStrikeParticleOptions(ParticleType<?> particleType, Vec3 direction, float size, int lifetime) {
        super(particleType, size, lifetime);
        this.direction = direction;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf p_123732_) {
        LIGHTNING_STREAM_CODEC.toNetwork(p_123732_, this);
    }
}
