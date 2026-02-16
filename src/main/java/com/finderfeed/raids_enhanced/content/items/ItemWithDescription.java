package com.finderfeed.raids_enhanced.content.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ItemWithDescription extends Item {

    private Component component;

    public ItemWithDescription(Properties p_41383_, Component component) {
        super(p_41383_);
        this.component = component;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
        p_41423_.add(component.copy());
    }

}
