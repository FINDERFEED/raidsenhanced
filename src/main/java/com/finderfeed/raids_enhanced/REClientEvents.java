package com.finderfeed.raids_enhanced;

import com.finderfeed.fdlib.FDClientHelpers;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.head.HeadBoneTransformation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRenderLayerOptions;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDEntityRendererBuilder;
import com.finderfeed.fdlib.systems.bedrock.models.FDModel;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.raids_enhanced.content.entities.electromancer.ElectromancerEntity;
import com.finderfeed.raids_enhanced.content.entities.electromancer.ElectromancerRenderer;
import com.finderfeed.raids_enhanced.content.entities.falling_block.REFallingBlockRenderer;
import com.finderfeed.raids_enhanced.content.entities.golem_of_last_resort.GolemOfLastResort;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaiderBomb;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons.RaidBlimpCannonBonesController;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons.RaidBlimpCannonProjectile;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts.RaidBlimpPartRenderer;
import com.finderfeed.raids_enhanced.content.particles.explosion_particle.RExplosionParticle;
import com.finderfeed.raids_enhanced.content.particles.lightning_explosion.LightningExplosionParticle;
import com.finderfeed.raids_enhanced.content.particles.lightning_strike.LightningStrikeParticle;
import com.finderfeed.raids_enhanced.init.REEntities;
import com.finderfeed.raids_enhanced.init.REModels;
import com.finderfeed.raids_enhanced.init.REParticles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = RaidsEnhanced.MOD_ID, value = Dist.CLIENT)
public class REClientEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(REParticles.EXPLOSION.get(), RExplosionParticle.Factory::new);
        event.registerSpriteSet(REParticles.LIGHTNING_EXPLOSION.get(), LightningExplosionParticle.Provider::new);
        event.registerSpriteSet(REParticles.LIGHTNING_STRIKE.get(), LightningStrikeParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){

        event.registerEntityRenderer(REEntities.FALLING_BLOCK.get(), REFallingBlockRenderer::new);

        event.registerEntityRenderer(REEntities.ELECTROMANCER.get(), FDEntityRendererBuilder.<ElectromancerEntity>builder()
                        .addLayer(FDEntityRenderLayerOptions.<ElectromancerEntity>builder()
                                .model(REModels.ELECTROMANCER)
                                .renderType(RenderType.entityCutoutNoCull(RaidsEnhanced.location("textures/entities/electromancer.png")))
                                .build())
                        .shouldRender(((electromancerEntity, frustum, v, v1, v2) -> {
                            return true;
                        }))
                        .freeRender(new ElectromancerRenderer())
                .build());

        event.registerEntityRenderer(REEntities.GOLEM_OF_LAST_RESORT.get(), FDEntityRendererBuilder.<GolemOfLastResort>builder()
                        .addLayer(FDEntityRenderLayerOptions.<GolemOfLastResort>builder()
                                .model(REModels.GOLEM_OF_LAST_RESORT)
                                .renderType(RenderType.entityCutoutNoCull(RaidsEnhanced.location("textures/entities/golem_of_last_resort.png")))
                                .addBoneController("head", new HeadBoneTransformation<>())
                                .build())
                        .addLayer(FDEntityRenderLayerOptions.<GolemOfLastResort>builder()
                                .model(REModels.GOLEM_OF_LAST_RESORT)
                                .renderType(RenderType.eyes(RaidsEnhanced.location("textures/entities/golem_of_last_resort_emissive.png")))
                                .addBoneController("head", new HeadBoneTransformation<>())
                                .build())
                .build());

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

        event.registerEntityRenderer(REEntities.BOMB.get(), FDEntityRendererBuilder.<RaiderBomb>builder()
                        .addLayer(FDEntityRenderLayerOptions.<RaiderBomb>builder()
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

        event.registerEntityRenderer(REEntities.RAID_AIRSHIP_PART.get(), RaidBlimpPartRenderer::new);

    }

}
