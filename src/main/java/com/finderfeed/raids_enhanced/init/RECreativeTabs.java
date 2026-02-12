package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RECreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RaidsEnhanced.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RaidsEnhanced.MOD_ID))
                    .icon(() -> new ItemStack(REItems.ENGINEER_STAFF.get()))
                    .displayItems((params, output) -> {
                        output.accept(REItems.ENGINEER_STAFF.get());
                        output.accept(REItems.PLAYER_BLIMP.get());
                        output.accept(REItems.BLIMP_PARTS.get());
                    })
                    .build()
    );

}
