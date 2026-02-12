package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.items.handcannon.HandCannon;
import com.finderfeed.raids_enhanced.content.items.PlayerBlimpItem;
import com.finderfeed.raids_enhanced.content.items.REDebugStick;
import com.finderfeed.raids_enhanced.content.items.electromancer_staff.EngineerStaff;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(RaidsEnhanced.MOD_ID);

    public static final Supplier<Item> BLIMP_PARTS = ITEMS.register("blimp_parts", ()->new Item(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> PLAYER_BLIMP = ITEMS.register("player_blimp", ()->new PlayerBlimpItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> DEBUG_STICK = ITEMS.register("debug_stick", ()->new REDebugStick(new Item.Properties().stacksTo(1)));
    public static final Supplier<EngineerStaff> ENGINEER_STAFF = ITEMS.register("engineer_staff", ()->new EngineerStaff(new Item.Properties().stacksTo(1)));
    public static final Supplier<HandCannon> HANDCANNON = ITEMS.register("handcannon", ()->new HandCannon(new Item.Properties().stacksTo(1)));

}
