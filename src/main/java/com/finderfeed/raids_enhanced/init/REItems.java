package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.items.REDebugStick;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(RaidsEnhanced.MOD_ID);

    public static final Supplier<Item> DEBUG_STICK = ITEMS.register("debug_stick", ()->new REDebugStick(new Item.Properties().stacksTo(1)));

}
