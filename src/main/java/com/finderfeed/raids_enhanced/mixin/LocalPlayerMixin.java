package com.finderfeed.raids_enhanced.mixin;

import com.finderfeed.raids_enhanced.content.entities.player_blimp.PlayerBlimpEntity;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Shadow public Input input;

    @Inject(method = "rideTick", at = @At("HEAD"))
    public void rideTick(CallbackInfo ci){
        LocalPlayer localPlayer = (LocalPlayer) (Object) this;
        if (localPlayer.getControlledVehicle() instanceof PlayerBlimpEntity playerBlimp) {
            playerBlimp.setInput(this.input.left, this.input.right, this.input.up, this.input.down, this.input.shiftKeyDown, this.input.jumping);
        }
    }


}
