package com.finderfeed.raids_enhanced.content.entities.raid_drill;

import com.finderfeed.fdlib.data_structures.Pair;
import com.finderfeed.fdlib.systems.bedrock.animations.Animation;
import com.finderfeed.fdlib.systems.bedrock.animations.TransitionAnimation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.entity.renderer.FDFreeEntityRenderer;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.fdlib.util.rendering.FDEasings;
import com.finderfeed.fdlib.util.rendering.FDRenderUtil;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;
import java.util.Random;

public class RaidDrillRenderer implements FDFreeEntityRenderer<RaidDrill> {

    public static final List<Pair<Vec3, Vec3>> PAIRS = List.of(
            new Pair<>(new Vec3(0.8,0,0.8), new Vec3(1,1,1)),
            new Pair<>(new Vec3(-0.8,0,-0.8), new Vec3(-1,0.8,-1)),
            new Pair<>(new Vec3(-0.8,0,0.8), new Vec3(-1,1,1)),
            new Pair<>(new Vec3(0.8,0,-0.8), new Vec3(1,0.8,-1)),

            new Pair<>(new Vec3(1.1,-0.1,0), new Vec3(1,0.5,0.2)),
            new Pair<>(new Vec3(-1.1,-0.1,0), new Vec3(-1,0.7,0.3)),
            new Pair<>(new Vec3(0,-0.1,-1.1), new Vec3(0.5,0.5,-1))
    );

    @Override
    public void render(RaidDrill raidDrill, float pticks, float yaw, PoseStack matrices, MultiBufferSource multiBufferSource, int light) {

        if (!raidDrill.blocksToRender.isEmpty()) {
            Random random = new Random(raidDrill.getId());
            var animSystem = raidDrill.getAnimationSystem();

            var ticker = animSystem.getTicker(RaidDrill.BURROW_LAYER);

            if (ticker != null) {

                float tickerTime = ticker.getTime(yaw);
                float animTime = ticker.getAnimation().getAnimTime();

                Animation animation = ticker.getAnimation();
                if (animation instanceof TransitionAnimation transitionAnimation){
                    animation = transitionAnimation.getTransitionTo();
                }

                float p;

                if (animation == REAnimations.RAIDER_DRILL_UNBURROW.get()){
                    p = tickerTime / animTime;
                }else if (animation == REAnimations.RAIDER_DRILL_BURROW.get()){
                    p = 1 - tickerTime / animTime;
                }else{
                    return;
                }

                p = FDEasings.easeOutBack(p);



                matrices.pushPose();
                matrices.mulPose(Axis.YP.rotationDegrees(-raidDrill.getYRot()));
                matrices.translate(0,-0.6 + 0.6 * p,0);

                for (var pair : PAIRS) {
                    BlockState state = raidDrill.blocksToRender.get(random.nextInt(raidDrill.blocksToRender.size()));

                    Vec3 directionTarget = pair.second;
                    Vec3 direction = FDMathUtil.interpolateVectors(new Vec3(directionTarget.x * 0.05,1,directionTarget.z * 0.05), directionTarget, p);


                    this.renderBlockWithRotationOrOffset(matrices, multiBufferSource, state, pair.first, direction, LightTexture.FULL_BRIGHT);
                }
                matrices.popPose();
            }
        }
    }

    private void renderBlockWithRotationOrOffset(PoseStack matrices, MultiBufferSource multiBufferSource, BlockState blockState, Vec3 offset, Vec3 lookDir, int light){
        matrices.pushPose();

        matrices.translate(-0.5,-0.5,-0.5);

        matrices.translate(offset.x, offset.y, offset.z);
        matrices.translate(0.5,0.5,0.5);
        FDRenderUtil.applyMovementMatrixRotations(matrices, lookDir);
        matrices.translate(-0.5,-0.5,-0.5);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        dispatcher.renderSingleBlock(blockState, matrices, multiBufferSource, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);

        matrices.popPose();
    }


}
