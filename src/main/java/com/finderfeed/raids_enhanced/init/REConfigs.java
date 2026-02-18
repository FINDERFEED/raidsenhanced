package com.finderfeed.raids_enhanced.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.config.JsonConfig;
import com.finderfeed.raids_enhanced.REConfig;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REConfigs {

    public static final DeferredRegister<JsonConfig> CONFIGS = DeferredRegister.create(FDRegistries.CONFIGS_KEY, RaidsEnhanced.MOD_ID);

    public static final Supplier<REConfig> CONFIG = CONFIGS.register("raidsenhanced", REConfig::new);

}
