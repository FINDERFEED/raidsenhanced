package com.finderfeed.raids_enhanced.init;

import com.finderfeed.fdlib.systems.FDRegistries;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class REAnimations {

    public static DeferredRegister<Animation> ANIMATIONS = DeferredRegister.create(FDRegistries.ANIMATIONS, RaidsEnhanced.MOD_ID);

    public static DeferredHolder<Animation,Animation> RAID_AIRSHIP_FLY = ANIMATIONS.register("raid_airship_fly", ()->{
        return new Animation(RaidsEnhanced.location("raid_airship"));
    });

    public static DeferredHolder<Animation,Animation> RAID_AIRSHIP_IDLE = ANIMATIONS.register("raid_airship_idle", ()->{
        return new Animation(RaidsEnhanced.location("raid_airship"));
    });

    public static DeferredHolder<Animation,Animation> RAID_AIRSHIP_ILLAGER_OBSERVE = ANIMATIONS.register("raid_airship_illager_observe", ()->{
        return new Animation(RaidsEnhanced.location("raid_airship"));
    });

    public static DeferredHolder<Animation,Animation> RAID_AIRSHIP_THROW_BOMB = ANIMATIONS.register("raid_airship_throw_bomb", ()->{
        return new Animation(RaidsEnhanced.location("raid_airship"));
    });

    public static DeferredHolder<Animation,Animation> RAID_AIRSHIP_DEATH = ANIMATIONS.register("raid_airship_death", ()->{
        return new Animation(RaidsEnhanced.location("raid_airship"));
    });


}
