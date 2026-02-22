package com.finderfeed.raids_enhanced.content.entities.raid_drill;

import com.finderfeed.fdlib.FDClientHelpers;
import com.finderfeed.fdlib.nbt.AutoSerializable;
import com.finderfeed.fdlib.nbt.SerializableField;
import com.finderfeed.fdlib.systems.bedrock.animations.TransitionAnimation;
import com.finderfeed.fdlib.systems.bedrock.animations.animation_system.AnimationTicker;
import com.finderfeed.fdlib.util.math.FDMathUtil;
import com.finderfeed.raids_enhanced.REClientUtil;
import com.finderfeed.raids_enhanced.REUtil;
import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.entities.FDRaider;
import com.finderfeed.raids_enhanced.init.REAnimations;
import com.finderfeed.raids_enhanced.init.REConfigs;
import com.finderfeed.raids_enhanced.init.RESounds;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class RaidDrill extends FDRaider implements AutoSerializable {

    public static final String BURROW_LAYER = "BURROWED";

    public static final EntityDataAccessor<Boolean> IS_VISIBLE = SynchedEntityData.defineId(RaidDrill.class, EntityDataSerializers.BOOLEAN);

    public static TagKey<Block> CANNOT_DIG_OUT_FROM = BlockTags.create(RaidsEnhanced.location("cannot_dig_out_from"));

    protected List<BlockState> blocksToRender = new ArrayList<>();

    @SerializableField
    public int reDigTicker = 0;

    @SerializableField
    public int hits = 0;

    @SerializableField
    public int raidersSpawningTicker = -1;

    @SerializableField
    private int raidersToSpawn = 0;

    @SerializableField
    private int idleTicker = 0;

    @SerializableField
    private int automaticReDigAmount = 0;

    private BlockPos noRaidPos;


    public RaidDrill(EntityType<? extends Raider> drill, Level level) {
        super(drill, level);
        this.setNoGravity(true);
        this.getAnimationSystem().startAnimation("IDLE", AnimationTicker.builder(REAnimations.RAIDER_DRILL_IDLE)
                .build());
        this.getAnimationSystem().startAnimation(BURROW_LAYER, AnimationTicker.builder(REAnimations.RAIDER_DRILL_UNBURROW)
                .build());
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide){

            if (noRaidPos == null){
                noRaidPos = this.getOnPos().offset(1,0,0);
            }

            if (this.getVehicle() != null){
                this.stopRiding();
            }
            this.tickReDig();
            this.tickRaidersSpawning();

        }else {
            if (tickCount % 4 == 0) {
                this.blocksToRender = REClientUtil.collectStates(level(), this.position().add(0,-0.5,0), 1);
            }

            AnimationTicker animationTicker = this.getAnimationSystem().getTicker(BURROW_LAYER);
            boolean shouldPlaySound = true;
            if (animationTicker != null){
                var animation = animationTicker.getAnimation();
                if (animation instanceof TransitionAnimation transitionAnimation){
                    animation = transitionAnimation.getTransitionTo();
                }
                if (animation == REAnimations.RAIDER_DRILL_BURROW.get()){
                    float time = animationTicker.getTime(1);
                    if (time > 0){
                        shouldPlaySound = false;
                    }
                }
            }

            if (shouldPlaySound && tickCount % 7 == 0){
                this.level().playSound(FDClientHelpers.getClientPlayer(), this.getX(), this.getY(), this.getZ(), RESounds.RAID_DRILL_IDLE.get(), SoundSource.HOSTILE, 1f, 1f);
            }
        }

    }

    public void tickRaidersSpawning(){
        if (this.raidersSpawningTicker != -1){

            if (this.raidersToSpawn > 0 && this.raidersSpawningTicker % 60 == 0){
                if (this.spawnRaider()){
                    this.raidersToSpawn--;
                }
            }else{
                idleTicker++;
                if (this.automaticReDigAmount < REConfigs.CONFIG.get().raidDrill.automaticBurrowTimes && idleTicker >= REConfigs.CONFIG.get().raidDrill.burrowAgainAfter){
                    this.automaticReDigAmount++;
                    this.launchReDig();
                }
            }

            this.raidersSpawningTicker++;
        }
    }

    private boolean spawnRaider(){
        EntityType<? extends Raider> type;
        if (random.nextInt(2) == 1){
            type = EntityType.VINDICATOR;
        }else{
            type = EntityType.PILLAGER;
        }
        Entity entity = type.create(level());
        if (entity instanceof Raider raider) {
            if (this.raid != null){
                this.raid.joinRaid(this.raid.getGroupsSpawned(), raider, this.getOnPos(), false);
            }else{
                entity.setPos(this.position());
                raider.finalizeSpawn((ServerLevel) this.level(), this.level().getCurrentDifficultyAt(this.getOnPos()), MobSpawnType.EVENT, null);
                level().addFreshEntity(raider);
            }
            return true;
        }
        return false;
    }

    public void tickReDig(){
        if (this.reDigTicker != -1){

            int burrowedTime = REAnimations.RAIDER_DRILL_BURROW.get().getAnimTime();

            int unburrowTime = burrowedTime + 10;

            if (this.reDigTicker >= burrowedTime && this.reDigTicker <= unburrowTime){
                this.setVisible(false);
            }else{
                this.setVisible(true);
            }

            if (this.reDigTicker == 0){
                this.getAnimationSystem().startAnimation(BURROW_LAYER, AnimationTicker.builder(REAnimations.RAIDER_DRILL_BURROW)
                        .build());
            } else if (this.reDigTicker == burrowedTime + 5){

                BlockPos relativePos;
                if (this.raid != null){
                    relativePos = this.raid.getCenter();
                }else{
                    if (noRaidPos == null){
                        noRaidPos = this.getOnPos().offset(1,0,0);
                    }
                    relativePos = this.noRaidPos;
                }

                BlockPos blockPos = this.calculateDigOutPos(relativePos);
                if (blockPos != null) {
                    this.teleportTo(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);

                    Vec3 lookAtPos;
                    if (this.raid != null){
                        lookAtPos = blockPos.getCenter();
                    }else{
                        lookAtPos = this.position().add(new Vec3(1,0,0).yRot(FDMathUtil.FPI * 2 * random.nextFloat()));
                    }

                    this.lookAt(EntityAnchorArgument.Anchor.FEET, lookAtPos);
                }
            } else if (this.reDigTicker == burrowedTime + 10){
                this.getAnimationSystem().startAnimation(BURROW_LAYER, AnimationTicker.builder(REAnimations.RAIDER_DRILL_UNBURROW)
                        .build());
            }else if (this.reDigTicker == burrowedTime + 12){
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), RESounds.RAID_DRILL_DIG_OUT.get(), SoundSource.HOSTILE, 5f, 1f);
            } else if (this.reDigTicker >= unburrowTime + REAnimations.RAIDER_DRILL_UNBURROW.get().getAnimTime() + 10){
                this.raidersSpawningTicker = 0;
                this.idleTicker = 0;
                this.raidersToSpawn = REConfigs.CONFIG.get().raidDrill.minRaidersSpawn + random.nextInt(REConfigs.CONFIG.get().raidDrill.maxRaidersSpawn - REConfigs.CONFIG.get().raidDrill.minRaidersSpawn + 1);
                this.reDigTicker = -1;
                return;
            }

            this.reDigTicker++;

        }else{
            this.setVisible(true);
            this.getAnimationSystem().startAnimation(BURROW_LAYER, AnimationTicker.builder(REAnimations.RAIDER_DRILL_UNBURROW)
                    .build());
        }
    }

    @Override
    public void checkDespawn() {
        if (net.neoforged.neoforge.event.EventHooks.checkMobDespawn(this)) return;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= 1 && !this.level().isClientSide() && !this.isRemoved()) {
            REUtil.drillDeath((ServerLevel) level(), this.position(), 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public boolean hurt(DamageSource src, float damage) {

        if (src.getEntity() != null){
            if (reDigTicker == -1 && super.hurt(src, 0.1f)){
                this.idleTicker = 0;
                this.automaticReDigAmount = 0;
                if (++hits >= 3){
                    int newHealth = (int) this.getHealth();
                    if (newHealth == 0){
                        this.setHealth(0);
                        this.die(src);
                    }else {
                        this.launchReDig();
                        this.setHealth(newHealth);
                    }
                }
                return true;
            }
        }else{
            return (src.is(DamageTypes.FELL_OUT_OF_WORLD) || src.is(DamageTypes.GENERIC_KILL)) && super.hurt(src, damage);
        }

        return false;
    }

    public boolean isVisible(){
        return this.getEntityData().get(IS_VISIBLE);
    }

    public void setVisible(boolean state){
        this.getEntityData().set(IS_VISIBLE, state);
    }

    private void launchReDig(){
        this.raidersToSpawn = 0;
        this.idleTicker = 0;
        this.raidersSpawningTicker = -1;
        this.reDigTicker = 0;
        hits = 0;
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
                && !level().getBlockState(blockPos).getCollisionShape(level(), blockPos).isEmpty()
                && level().getBlockState(blockPos.above()).getCollisionShape(level(), blockPos.above()).isEmpty()
                && level().getBlockState(blockPos.above(2)).getCollisionShape(level(), blockPos.above(2)).isEmpty()
        ){
            return blockPos;
        }

        return null;
    }

    private boolean isBlockNotValidForSpawn(BlockState blockState){
        return blockState.is(CANNOT_DIG_OUT_FROM);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33034_) {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_VISIBLE, false);
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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.autoSave(tag);
        if (this.noRaidPos != null) {
            tag.putInt("noRaidPosX", this.noRaidPos.getX());
            tag.putInt("noRaidPosY", this.noRaidPos.getY());
            tag.putInt("noRaidPosZ", this.noRaidPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.autoLoad(tag);
        if (tag.contains("noRaidPosX")){
            this.noRaidPos = new BlockPos(
                    tag.getInt("noRaidPosX"),
                    tag.getInt("noRaidPosY"),
                    tag.getInt("noRaidPosZ")
            );
        }
    }

    @EventBusSubscriber
    public static class Events {

        @SubscribeEvent
        public static void effectEvent(MobEffectEvent.Applicable event){
            if (event.getEntity() instanceof RaidDrill raidDrill && event.getEffectInstance() != null && !event.getEffectInstance().is(MobEffects.GLOWING)){
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }

        @SubscribeEvent
        public static void targetEvent(LivingChangeTargetEvent event){
            if (event.getNewAboutToBeSetTarget() instanceof RaidDrill){
                event.setCanceled(true);
            }
        }

    }

}
