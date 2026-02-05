package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.particles.explosion_particle.RExplosionParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, RaidsEnhanced.MOD_ID);

    public static final Supplier<ParticleType<RExplosionParticleOptions>> EXPLOSION = PARTICLES.register("explosion", () -> new ParticleType<RExplosionParticleOptions>(true) {
        @Override
        public MapCodec<RExplosionParticleOptions> codec() {
            return RExplosionParticleOptions.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RExplosionParticleOptions> streamCodec() {
            return RExplosionParticleOptions.STREAM_CODEC;
        }
    });

}
