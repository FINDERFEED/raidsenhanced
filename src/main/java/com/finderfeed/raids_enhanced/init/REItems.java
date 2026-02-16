package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.items.ItemWithDescription;
import com.finderfeed.raids_enhanced.content.items.handcannon.HandCannon;
import com.finderfeed.raids_enhanced.content.items.PlayerBlimpItem;
import com.finderfeed.raids_enhanced.content.items.REDebugStick;
import com.finderfeed.raids_enhanced.content.items.electromancer_staff.ZapperStaff;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class REItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(RaidsEnhanced.MOD_ID);

    public static final Supplier<Item> BLIMP_PARTS = ITEMS.register("blimp_parts", ()->new ItemWithDescription(new Item.Properties().stacksTo(1), Component.translatable("raidsenhanced.item_description.blimp_parts").withStyle(ChatFormatting.GOLD)));
    public static final Supplier<Item> PLAYER_BLIMP = ITEMS.register("player_blimp", ()->new PlayerBlimpItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> DEBUG_STICK = ITEMS.register("debug_stick", ()->new REDebugStick(new Item.Properties().stacksTo(1)));
    public static final Supplier<ZapperStaff> ZAPPER_STAFF = ITEMS.register("zapper_staff", ()->new ZapperStaff(new Item.Properties().stacksTo(1)));
    public static final Supplier<HandCannon> HANDCANNON = ITEMS.register("handcannon", ()->new HandCannon(new Item.Properties().stacksTo(1)));

}
