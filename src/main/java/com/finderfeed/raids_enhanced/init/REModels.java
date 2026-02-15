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
    public static final Supplier<FDModelInfo> RAID_BLIMP_BOMB = MODELS.register("raid_airship_bomb",()->new FDModelInfo(RaidsEnhanced.location("raid_airship_bomb"),1f));
    public static final Supplier<FDModelInfo> RAID_AIRSHIP_PART_1 = MODELS.register("raid_airship_part_1",()->new FDModelInfo(RaidsEnhanced.location("raid_airship_part_1"),1f));
    public static final Supplier<FDModelInfo> RAID_AIRSHIP_PART_2 = MODELS.register("raid_airship_part_2",()->new FDModelInfo(RaidsEnhanced.location("raid_airship_part_2"),1f));

    public static final Supplier<FDModelInfo> GOLEM_OF_LAST_RESORT = MODELS.register("golem_of_last_resort",()->new FDModelInfo(RaidsEnhanced.location("golem_of_last_resort"),1f));

    public static final Supplier<FDModelInfo> ELECTROMANCER = MODELS.register("electromancer",()->new FDModelInfo(RaidsEnhanced.location("electromancer"),1f));

    public static final Supplier<FDModelInfo> PLAYER_BLIMP = MODELS.register("player_blimp",()->new FDModelInfo(RaidsEnhanced.location("player_blimp"),1f));

    public static final Supplier<FDModelInfo> RAID_DRILL = MODELS.register("raider_drill",()->new FDModelInfo(RaidsEnhanced.location("raider_drill"),1f));

}
