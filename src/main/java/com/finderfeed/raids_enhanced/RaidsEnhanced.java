package com.finderfeed.raids_enhanced;

import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REEntities;
import com.finderfeed.raids_enhanced.init.REItems;
import com.finderfeed.raids_enhanced.init.REModels;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;


@Mod(RaidsEnhanced.MOD_ID)
public class RaidsEnhanced {

    public static final String MOD_ID = "raidsenhanced";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation location(String loc){
        return ResourceLocation.tryBuild(MOD_ID,loc);
    }

    public RaidsEnhanced(IEventBus modEventBus, ModContainer modContainer) {
        REAnimations.ANIMATIONS.register(modEventBus);
        REEntities.ENTITIES.register(modEventBus);
        REModels.MODELS.register(modEventBus);
        REItems.ITEMS.register(modEventBus);
    }

}
