package com.finderfeed.raids_enhanced.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.bedrock.models.FDModelInfo;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REModels {

    public static final DeferredRegister<FDModelInfo> MODELS = DeferredRegister.create(FDRegistries.MODELS, RaidsEnhanced.MOD_ID);

    public static final Supplier<FDModelInfo> RAID_BLIMP = MODELS.register("raid_airship",()->new FDModelInfo(RaidsEnhanced.location("raid_airship"),1f));
    public static final Supplier<FDModelInfo> RAID_BLIMP_CANNON_PROJECTILE = MODELS.register("raid_blimp_cannon_projectile",()->new FDModelInfo(RaidsEnhanced.location("raid_blimp_cannon_projectile"),1f));

}
