package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.particles.SimpleTexturedParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticleOptions;
import com.finderfeed.raids_enhanced.content.particles.slash_particle.SlashParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, RaidsEnhanced.MOD_ID);

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> EXPLOSION = PARTICLES.register("explosion", () -> new ParticleType<SimpleTexturedParticleOptions>(true) {
        @Override
        public MapCodec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.mapCodec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SimpleTexturedParticleOptions> streamCodec() {
            return SimpleTexturedParticleOptions.streamCodec(this);
        }
    });

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> LIGHTNING_EXPLOSION = PARTICLES.register("lightning_explosion", () -> new ParticleType<SimpleTexturedParticleOptions>(true) {
        @Override
        public MapCodec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.mapCodec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SimpleTexturedParticleOptions> streamCodec() {
            return SimpleTexturedParticleOptions.streamCodec(this);
        }
    });

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> VERTICAL_LIGHTNING = PARTICLES.register("vertical_lightning", () -> new ParticleType<SimpleTexturedParticleOptions>(true) {
        @Override
        public MapCodec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.mapCodec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SimpleTexturedParticleOptions> streamCodec() {
            return SimpleTexturedParticleOptions.streamCodec(this);
        }
    });

    public static final Supplier<ParticleType<SimpleTexturedParticleOptions>> BALL_LIGHTNING_EXPLOSION = PARTICLES.register("ball_lightning_explosion", () -> new ParticleType<SimpleTexturedParticleOptions>(true) {
        @Override
        public MapCodec<SimpleTexturedParticleOptions> codec() {
            return SimpleTexturedParticleOptions.mapCodec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SimpleTexturedParticleOptions> streamCodec() {
            return SimpleTexturedParticleOptions.streamCodec(this);
        }
    });

    public static final Supplier<ParticleType<LightningStrikeParticleOptions>> LIGHTNING_STRIKE = PARTICLES.register("lightning_strike", () -> new ParticleType<LightningStrikeParticleOptions>(true) {
        @Override
        public MapCodec<LightningStrikeParticleOptions> codec() {
            return LightningStrikeParticleOptions.lightningMapCodec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, LightningStrikeParticleOptions> streamCodec() {
            return LightningStrikeParticleOptions.lightningStreamCodec(this);
        }
    });

    public static final Supplier<ParticleType<SlashParticleOptions>> ELECTRIC_SLASH = PARTICLES.register("electric_slash", () -> new ParticleType<SlashParticleOptions>(true) {
        @Override
        public MapCodec<SlashParticleOptions> codec() {
            return SlashParticleOptions.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, SlashParticleOptions> streamCodec() {
            return SlashParticleOptions.streamCodec(this);
        }
    });

}
