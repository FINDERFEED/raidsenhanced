package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RESounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, RaidsEnhanced.MOD_ID);

    public static final Supplier<SoundEvent> RAID_BLIMP_FALL = SOUNDS.register("raid_blimp_falling", ()->SoundEvent.createVariableRangeEvent(RaidsEnhanced.location("raid_blimp_falling")));

}
