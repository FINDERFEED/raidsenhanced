package com.finderfeed.raids_enhanced;

import com.finderfeed.raids_enhanced.init.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;


@Mod(RaidsEnhanced.MOD_ID)
public class RaidsEnhanced {

    public static final String MOD_ID = "raidsenhanced";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation location(String loc){
        return ResourceLocation.tryBuild(MOD_ID,loc);
    }

    public RaidsEnhanced() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REAnimations.ANIMATIONS.register(modEventBus);
        REParticles.PARTICLES.register(modEventBus);
        RECreativeTabs.TABS.register(modEventBus);
        REEntities.ENTITIES.register(modEventBus);
        REConfigs.CONFIGS.register(modEventBus);
        REModels.MODELS.register(modEventBus);
        RESounds.SOUNDS.register(modEventBus);
        REItems.ITEMS.register(modEventBus);
    }

}
