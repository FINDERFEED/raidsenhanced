package com.finderfeed.raids_enhanced.mixin;

import com.finderfeed.raids_enhanced.REClientMixinHandler;
import com.finderfeed.raids_enhanced.content.entities.player_blimp.PlayerBlimpEntity;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GameNarrator;sayNow(Lnet/minecraft/network/chat/Component;)V", shift = At.Shift.AFTER), cancellable = true)
    public void handleSetEntityPassengers(ClientboundSetPassengersPacket packet, CallbackInfo ci){
        REClientMixinHandler.setPassengers(level, packet,ci);
    }

}
