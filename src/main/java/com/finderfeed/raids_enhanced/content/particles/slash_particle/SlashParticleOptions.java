package com.finderfeed.raids_enhanced.content.particles.slash_particle;

import com.finderfeed.fdlib.systems.stream_codecs.NetworkCodec;
import com.finderfeed.fdlib.util.FDCodecs;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class SlashParticleOptions implements ParticleOptions {

    public static final Deserializer<SlashParticleOptions> DESERIALIZER = new Deserializer<SlashParticleOptions>() {
        @Override
        public SlashParticleOptions fromCommand(ParticleType<SlashParticleOptions> p_123733_, StringReader p_123734_) throws CommandSyntaxException {
            return new SlashParticleOptions(REParticles.ELECTRIC_SLASH.get(), new Vec3(0,1,0),1,1,1,true);
        }

        @Override
        public SlashParticleOptions fromNetwork(ParticleType<SlashParticleOptions> p_123735_, FriendlyByteBuf p_123736_) {
            return NETWORK_CODEC.fromNetwork(p_123736_);
        }
    };

    public static NetworkCodec<SlashParticleOptions> NETWORK_CODEC = NetworkCodec.composite(
            NetworkCodec.registry(()->ForgeRegistries.PARTICLE_TYPES), v->v.particleType,
            NetworkCodec.VEC3, v->v.direction,
            NetworkCodec.INT, v->v.lifetime,
            NetworkCodec.FLOAT, v->v.pitch,
            NetworkCodec.FLOAT, v->v.size,
            NetworkCodec.BOOL, v->v.reversedSide,
            SlashParticleOptions::new
    );

    public static Codec<SlashParticleOptions> CODEC = RecordCodecBuilder.create(p->p.group(
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particle_type").forGetter(v->v.particleType),
            FDCodecs.VEC3.fieldOf("direction").forGetter(v->v.direction),
            Codec.INT.fieldOf("lifetime").forGetter(v->v.lifetime),
            Codec.FLOAT.fieldOf("pitch").forGetter((v->v.pitch)),
            Codec.FLOAT.fieldOf("size").forGetter((v->v.size)),
            Codec.BOOL.fieldOf("reversedSide").forGetter((v->v.reversedSide))
    ).apply(p,SlashParticleOptions::new));

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        NETWORK_CODEC.toNetwork(buf, this);
    }

    @Override
    public String writeToString() {
        return "";
    }

}
