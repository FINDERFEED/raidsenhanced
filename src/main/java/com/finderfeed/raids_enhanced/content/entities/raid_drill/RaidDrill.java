package com.finderfeed.raids_enhanced.content.entities.raid_drill;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class RaidDrill extends FDRaider {

    public static TagKey<Block> CANNOT_DIG_OUT_FROM = BlockTags.create(RaidsEnhanced.location("cannot_dig_out_from"));

    public BlockPos testRaidPos;

    public RaidDrill(EntityType<? extends Raider> drill, Level level) {
        super(drill, level);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide){
            if (testRaidPos == null){
                this.remove(RemovalReason.DISCARDED);
            }else{
                if (tickCount % 50 == 0){
                    BlockPos blockPos = this.calculateDigOutPos(testRaidPos);
                    if (blockPos != null) {
                        this.teleportTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
                    }
                }
                this.addEffect(new MobEffectInstance(MobEffects.GLOWING,20,1));
            }
        }

    }

    @Override
    public boolean hurt(DamageSource src, float damage) {
        if (super.hurt(src, 0.01f)){
            if (testRaidPos != null) {
                BlockPos blockPos = this.calculateDigOutPos(testRaidPos);
                if (blockPos != null) {
                    this.teleportTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
                }
            }
        }
        return false;
    }

    private BlockPos calculateDigOutPos(BlockPos origin){
        Vec3 pos = this.position();
        Vec3 center = origin.getCenter();
        Vec3 between = center.subtract(pos);

        List<Vector2i> candidates = new ArrayList<>();
        int maxRadius = 25;
        int minRadius = 15;

        for (int x = -maxRadius; x <= maxRadius; x++){
            for (int z = -maxRadius; z <= maxRadius; z++){
                if (Math.abs(x) < minRadius && Math.abs(z) < minRadius) continue;
                double dot = x * between.x + z * between.z;
                if (dot > 0){
                    candidates.add(new Vector2i(x, z));
                }
            }
        }


        List<BlockPos> positions = new ArrayList<>();
        var iterator = candidates.iterator();
        while (iterator.hasNext()){
            var p = iterator.next();
            Vector2i v = new Vector2i(p.x + origin.getX(), p.y + origin.getZ());
            BlockPos ps = this.isValidDigOutPos(v);
            if (ps == null){
                iterator.remove();
            }else{
                positions.add(ps);
            }
        }

        if (positions.isEmpty()){
            return null;
        }else{
            return positions.get(random.nextInt(positions.size()));
        }

    }

    public BlockPos isValidDigOutPos(Vector2i vec){
        int height = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, vec.x, vec.y);
        BlockPos blockPos = new BlockPos(vec.x, height - 1, vec.y);

        BlockState b = level().getBlockState(blockPos);
        FluidState bf = level().getFluidState(blockPos);

        if (bf.isEmpty() && !this.isBlockNotValidForSpawn(b)
                && level().getBlockState(blockPos.above()).getCollisionShape(level(), blockPos.above()).isEmpty()
                && level().getBlockState(blockPos.above(2)).getCollisionShape(level(), blockPos.above(2)).isEmpty()
        ){
            return blockPos;
        }

        return null;
    }

    private boolean isBlockNotValidForSpawn(BlockState blockState){
        Block block = blockState.getBlock();
//        return blockState.is(BlockTags.PLANKS) || blockState.is(BlockTags.FENCES) || blockState.is(Blocks.COBBLESTONE);
        return blockState.is(CANNOT_DIG_OUT_FROM);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource src) {
        return super.isInvulnerableTo(src) || src.is(DamageTypes.IN_WALL);
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_) {

    }

    @Override
    protected boolean canRide(Entity p_20339_) {
        return false;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return null;
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    public void setDeltaMovement(Vec3 p_20257_) {

    }

    @Override
    public void setDeltaMovement(double p_20335_, double p_20336_, double p_20337_) {

    }

}
