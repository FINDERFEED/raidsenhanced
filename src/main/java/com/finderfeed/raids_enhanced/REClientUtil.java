package com.finderfeed.raids_enhanced;

import com.finderfeed.fdlib.FDClientHelpers;
import com.finderfeed.raids_enhanced.content.util.HorizontalCircleRandomDirections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class REClientUtil {

    public static void handlePosEvent(Vec3 pos, int event, int data){
        switch (event){
            case REUtil.GOLEM_SMACK -> {
                golemSmack(pos, data);
            }
        }
    }

    public static void golemSmack(Vec3 pos, int data){

        var level = FDClientHelpers.getClientLevel();

        var states = collectStates(level, pos, 2);
        if (states.isEmpty()){
            states.add(Blocks.STONE.defaultBlockState());
        }

        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;


        for (int i = 0; i < 5; i++){
            for (var dir : new HorizontalCircleRandomDirections(level.random, i * 5, 1f)){

                float strength = i * 0.05f + level.random.nextFloat() * 0.05f;
                float vspeed = level.random.nextFloat() * 0.2f + 0.05f;

                Vec3 ppos = pos.add(dir.scale(level.random.nextFloat())).add(0,0.2,0);
                Vec3 pspeed = dir.scale(strength).add(0,vspeed, 0);

                var particle = particleEngine.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, states.get(level.random.nextInt(states.size()))),
                        ppos.x,ppos.y,ppos.z,
                        pspeed.x,pspeed.y,pspeed.z);
                if (particle != null) {
                    particle.setParticleSpeed(pspeed.x,pspeed.y,pspeed.z);
                }
            }
        }

    }

    private static List<BlockState> collectStates(Level level, Vec3 pos, int radius){

        BlockPos blockPos = new BlockPos(
                (int) Math.floor(pos.x),
                (int) Math.floor(pos.y),
                (int) Math.floor(pos.z)
        );

        List<BlockState> blockStates = new ArrayList<>();


        for (int x = -radius; x <= radius; x++){
            for (int y = -radius; y <= radius; y++){
                for (int z = -radius; z <= radius; z++){

                    BlockPos offset = blockPos.offset(x,y,z);

                    BlockState blockState = level.getBlockState(offset);

                    if (!blockState.isAir() && blockState.isCollisionShapeFullBlock(level, offset)){
                        blockStates.add(blockState);
                    }

                }
            }
        }

        return blockStates;
    }


}
