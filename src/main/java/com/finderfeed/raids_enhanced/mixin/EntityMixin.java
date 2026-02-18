package com.finderfeed.raids_enhanced.mixin;

import com.finderfeed.raids_enhanced.content.entities.player_blimp.PlayerBlimpEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "canEnterPose", at = @At("HEAD"), cancellable = true)
    public void canEnterPose(Pose pose, CallbackInfoReturnable<Boolean> cir){
        Entity entity = (Entity) (Object) this;
        var vehicle = entity.getVehicle();
        if (vehicle instanceof PlayerBlimpEntity blimp && pose == Pose.CROUCHING){
            cir.setReturnValue(false);
        }
    }


}
