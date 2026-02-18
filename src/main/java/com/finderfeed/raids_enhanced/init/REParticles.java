package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.slash_particle.SlashParticleOptions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, RaidsEnhanced.MOD_ID);

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> EXPLOSION = PARTICLES.register("explosion", () -> new ParticleType<SimpleTexturedParticleOptions>(true, SimpleTexturedParticleOptions.DESERIALIZER) {
        @Override
        public Codec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.CODEC;
        }
    });

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> LIGHTNING_EXPLOSION = PARTICLES.register("lightning_explosion", () -> new ParticleType<SimpleTexturedParticleOptions>(true, SimpleTexturedParticleOptions.DESERIALIZER) {
        @Override
        public Codec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.CODEC;
        }
    });

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> VERTICAL_LIGHTNING = PARTICLES.register("vertical_lightning", () -> new ParticleType<SimpleTexturedParticleOptions>(true, SimpleTexturedParticleOptions.DESERIALIZER) {
        @Override
        public Codec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.CODEC;
        }
    });

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> BALL_LIGHTNING_EXPLOSION = PARTICLES.register("ball_lightning_explosion", () -> new ParticleType<SimpleTexturedParticleOptions>(true, SimpleTexturedParticleOptions.DESERIALIZER) {
        @Override
        public Codec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.CODEC;
        }
    });

    public static final Supplier<ParticleType<LightningStrikeParticleOptions>> LIGHTNING_STRIKE = PARTICLES.register("lightning_strike", () -> new ParticleType<LightningStrikeParticleOptions>(true, LightningStrikeParticleOptions.LIGHTNING_DESERIALIZER) {
        @Override
        public Codec<LightningStrikeParticleOptions> codec() {
            return LightningStrikeParticleOptions.LIGHTNING_CODEC;
        }

    });

    public static final Supplier<ParticleType<SlashParticleOptions>> ELECTRIC_SLASH = PARTICLES.register("electric_slash", () -> new ParticleType<SlashParticleOptions>(true, SlashParticleOptions.DESERIALIZER) {
        @Override
        public Codec<SlashParticleOptions> codec() {
            return SlashParticleOptions.CODEC;
        }

    });

}
