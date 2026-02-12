package com.finderfeed.raids_enhanced.mixin;

import com.finderfeed.raids_enhanced.REMixinHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public class RaidMixin {

    @Shadow private int groupsSpawned;

    @Shadow @Final private int numGroups;

    @Inject(method = "spawnGroup", at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;", shift = At.Shift.BEFORE))
    public void spawnGroup(BlockPos pos, CallbackInfo ci){
        REMixinHandler.raidMixin((Raid) (Object) this, pos, this.groupsSpawned + 1, numGroups);
    }

}
