package com.finderfeed.raids_enhanced;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRenderLayerOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRendererBuilder;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.init.REEntities;
import com.finderfeed.raids_enhanced.init.REModels;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = RaidsEnhanced.MOD_ID, value = Dist.CLIENT)
public class REClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){

        event.registerEntityRenderer(REEntities.RAID_BLIMP.get(), FDEntityRendererBuilder.<RaidBlimp>builder()
                        .addLayer(FDEntityRenderLayerOptions.<RaidBlimp>builder()
                                .model(REModels.RAID_BLIMP)
                                .renderType(RenderType.entityCutoutNoCull(RaidsEnhanced.location("textures/entities/raid_airship.png")))
                                .build())
                .build());

    }

}
