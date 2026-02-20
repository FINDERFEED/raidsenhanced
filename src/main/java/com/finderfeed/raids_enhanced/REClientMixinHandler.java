package com.finderfeed.raids_enhanced;

import com.finderfeed.raids_enhanced.content.entities.player_blimp.PlayerBlimpEntity;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class REClientMixinHandler {
    public static void setPassengers(ClientLevel level, ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        int vehicle = packet.getVehicle();
        var entity = level.getEntity(vehicle);
        if (entity instanceof PlayerBlimpEntity playerBlimp){
            Minecraft minecraft = Minecraft.getInstance();
            var component = Component.translatable("raidsenhanced.mount").withStyle(ChatFormatting.RED);
            minecraft.gui.setOverlayMessage(component, false);
            minecraft.getNarrator().sayNow(component);
        }
    }
}
