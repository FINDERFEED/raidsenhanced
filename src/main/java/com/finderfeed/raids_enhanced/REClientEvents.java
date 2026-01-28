package com.finderfeed.raids_enhanced;

import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRenderLayerOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRendererBuilder;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimpBomb;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimpCannonBonesController;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimpCannonProjectile;
import com.finderfeed.raids_enhanced.init.REEntities;
import com.finderfeed.raids_enhanced.init.REModels;
import com.mojang.math.Axis;
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
                                .addBoneController("airship", new RaidBlimpCannonBonesController())
                                .build())
                        .addLayer(FDEntityRenderLayerOptions.<RaidBlimp>builder()
                                .model(REModels.RAID_BLIMP)
                                .renderType(RenderType.eyes(RaidsEnhanced.location("textures/entities/raid_airship_emissive.png")))
                                .build())

                .build());

        event.registerEntityRenderer(REEntities.RAID_BLIMP_CANNON_PROJECTILE.get(), FDEntityRendererBuilder.<RaidBlimpCannonProjectile>builder()
                        .addLayer(FDEntityRenderLayerOptions.<RaidBlimpCannonProjectile>builder()
                                .model(REModels.RAID_BLIMP_CANNON_PROJECTILE)
                                .renderType(RenderType.entityCutoutNoCull(RaidsEnhanced.location("textures/entities/raid_blimp_cannon_projectile.png")))
                                .transformation(((entity, poseStack, v) -> {
                                    var deltaMovement = entity.getDeltaMovement();
                                    poseStack.translate(0,entity.getBbHeight()/2,0);
                                    FDRenderUtil.applyMovementMatrixRotations(poseStack,deltaMovement);
                                }))
                                .build())
                .build());

        event.registerEntityRenderer(REEntities.RAID_BLIMP_BOMB.get(), FDEntityRendererBuilder.<RaidBlimpBomb>builder()
                        .addLayer(FDEntityRenderLayerOptions.<RaidBlimpBomb>builder()
                                .model(REModels.RAID_BLIMP_BOMB)
                                .renderType(RenderType.entityCutoutNoCull(RaidsEnhanced.location("textures/entities/raid_airship_bomb.png")))
                                .transformation(((entity, poseStack, v) -> {
                                    var deltaMovement = entity.getDeltaMovement();
                                    float time = (entity.tickCount + v);
                                    poseStack.translate(0,entity.getBbHeight()/2,0);
                                    FDRenderUtil.applyMovementMatrixRotations(poseStack,deltaMovement);
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-time * 30));
                                }))
                                .build())
                .build());

    }

}
