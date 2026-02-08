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

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_STRIKE_1 = ANIMATIONS.register("illager_golem_strike_1", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_STRIKE_2 = ANIMATIONS.register("illager_golem_strike_2", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_IDLE = ANIMATIONS.register("illager_golem_idle", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_HEAVY_STRIKE = ANIMATIONS.register("illager_golem_heavy_strike", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_WHIRLWIND = ANIMATIONS.register("illager_golem_whirlwind", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_BOMBS = ANIMATIONS.register("illager_golem_bombs", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_WALK = ANIMATIONS.register("illager_golem_walk", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ILLAGER_GOLEM_WALK_NO_HANDS = ANIMATIONS.register("illager_golem_walk_no_hands", ()->{
        return new Animation(RaidsEnhanced.location("golem_of_last_resort"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_WALK_NO_HANDS = ANIMATIONS.register("electromancer_walk_no_hands", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_WALK = ANIMATIONS.register("electromancer_walk", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_ATTACK_1 = ANIMATIONS.register("electromancer_attack_1", ()-> {
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_ATTACK_2 = ANIMATIONS.register("electromancer_attack_2", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_IDLE = ANIMATIONS.register("electromancer_idle", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_RAY_CHARGE = ANIMATIONS.register("electromancer_ray_charge", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_RAY_CAST = ANIMATIONS.register("electromancer_ray_cast", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_RAY_CAST_STOP = ANIMATIONS.register("electromancer_ray_cast_stop", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });

    public static DeferredHolder<Animation,Animation> ELECTROMANCER_CHARGE_STICK = ANIMATIONS.register("electromancer_charge_stick", ()->{
        return new Animation(RaidsEnhanced.location("electromancer"));
    });


}
