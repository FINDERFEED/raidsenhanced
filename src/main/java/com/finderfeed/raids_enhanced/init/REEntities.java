package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.entities.ball_lightning.BallLightningEntity;
import com.finderfeed.raids_enhanced.content.entities.engineer.ZapperIllager;
import com.finderfeed.raids_enhanced.content.entities.falling_block.REFallingBlock;
import com.finderfeed.raids_enhanced.content.entities.golem_of_last_resort.GolemOfLastResort;
import com.finderfeed.raids_enhanced.content.entities.player_blimp.PlayerBlimpEntity;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaiderBomb;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.cannons.RaidBlimpCannonProjectile;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.raid_airship_parts.RaidBlimpPart;
import com.finderfeed.raids_enhanced.content.entities.raid_drill.RaidDrill;
import com.finderfeed.raids_enhanced.content.entities.vertical_lightning_strike.VerticalLightningStrikeAttack;
import com.finderfeed.raids_enhanced.content.items.electromancer_staff.ZapperStaffCastEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Random;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = RaidsEnhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class REEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, RaidsEnhanced.MOD_ID);

    public static final Supplier<EntityType<RaidBlimp>> RAID_BLIMP = ENTITIES.register("raid_blimp",()->EntityType.Builder.<RaidBlimp>of(
                    RaidBlimp::new, MobCategory.MONSTER
            )
            .sized(3f,4f)
            .build("raid_blimp"));

    public static final Supplier<EntityType<RaidBlimpCannonProjectile>> RAID_BLIMP_CANNON_PROJECTILE = ENTITIES.register("raid_blimp_cannon_projectile",()->EntityType.Builder.<RaidBlimpCannonProjectile>of(
                    RaidBlimpCannonProjectile::new, MobCategory.MISC
            )
            .sized(0.25f,0.25f)
            .build("raid_blimp_cannon_projectile"));

    public static final Supplier<EntityType<RaiderBomb>> BOMB = ENTITIES.register("raid_blimp_bomb",()->EntityType.Builder.<RaiderBomb>of(
                    RaiderBomb::new, MobCategory.MISC
            )
            .sized(0.5f,0.5f)
            .build("raid_blimp_bomb"));

    public static final Supplier<EntityType<RaidBlimpPart>> RAID_AIRSHIP_PART = ENTITIES.register("raid_airship_part",()->EntityType.Builder.<RaidBlimpPart>of(
                    RaidBlimpPart::new, MobCategory.MISC
            )
            .sized(0.5f,0.5f)
            .build("raid_airship_part"));

    public static final Supplier<EntityType<GolemOfLastResort>> GOLEM_OF_LAST_RESORT = ENTITIES.register("golem_of_last_resort",()->EntityType.Builder.<GolemOfLastResort>of(
                    GolemOfLastResort::new, MobCategory.MONSTER
            )
            .sized(1.4f,2.6f)
            .build("golem_of_last_resort"));


    public static final Supplier<EntityType<REFallingBlock>> FALLING_BLOCK = ENTITIES.register("falling_block",()->EntityType.Builder.<REFallingBlock>of(
                    REFallingBlock::new, MobCategory.MISC
            )
            .sized(0.2f,0.2f)
            .build("falling_block"));


    public static final Supplier<EntityType<VerticalLightningStrikeAttack>> VERTICAL_LIGHTNING = ENTITIES.register("vertical_lightning_strike_attack",()->EntityType.Builder.<VerticalLightningStrikeAttack>of(
                    VerticalLightningStrikeAttack::new, MobCategory.MISC
            )
            .sized(0.25f,0.25f)
            .build("falling_block"));


    public static final Supplier<EntityType<ZapperStaffCastEntity>> ENGINEER_STAFF_CAST_ENTTITY = ENTITIES.register("engineer_staff_cast_entity",()->EntityType.Builder.<ZapperStaffCastEntity>of(
                    ZapperStaffCastEntity::new, MobCategory.MISC
            )
            .sized(0.25f,0.25f)
            .build("engineer_staff_cast_entity"));


    public static final Supplier<EntityType<BallLightningEntity>> BALL_LIGHTNING = ENTITIES.register("ball_lightning",()->EntityType.Builder.<BallLightningEntity>of(
                    BallLightningEntity::new, MobCategory.MISC
            )
            .sized(0.5f,0.5f)
            .build("ball_lightning"));


    public static final Supplier<EntityType<ZapperIllager>> ZAPPER_ILLAGER = ENTITIES.register("zapper",()->EntityType.Builder.<ZapperIllager>of(
                    ZapperIllager::new, MobCategory.MONSTER
            )
            .sized(0.6F, 1.95F)
            .build("engineer"));


    public static final Supplier<EntityType<RaidDrill>> RAID_DRILL = ENTITIES.register("raid_drill",()->EntityType.Builder.<RaidDrill>of(
                    RaidDrill::new, MobCategory.MONSTER
            )
            .sized(1.2F, 2F)
            .build("raid_drill"));


    public static final Supplier<EntityType<PlayerBlimpEntity>> PLAYER_BLIMP = ENTITIES.register("player_blimp",()->EntityType.Builder.<PlayerBlimpEntity>of(
                    PlayerBlimpEntity::new, MobCategory.MISC
            )
            .sized(1.375F, 0.5625F)
            .clientTrackingRange(10)
            .build("electromancer_entity"));


    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        Random random = new Random();
        random.nextFloat(1, 2);
        event.put(RAID_BLIMP.get(), RaidBlimp.createMonsterAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 7.0)
                        .add(Attributes.MAX_HEALTH, 200)
                        .add(Attributes.FOLLOW_RANGE, 40.0)
                        .add(Attributes.FLYING_SPEED, 0.3f)
                .build());


        event.put(GOLEM_OF_LAST_RESORT.get(), IronGolem.createAttributes()
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.FOLLOW_RANGE, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3f)
                .add(Attributes.ARMOR, 8.0)
                .build());

        event.put(ZAPPER_ILLAGER.get(), Pillager.createAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 7)
                        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8f)
                        .add(Attributes.MAX_HEALTH, 100)
                        .add(Attributes.ARMOR, 8.0)
                .build());


        event.put(RAID_DRILL.get(), Pillager.createAttributes()
                        .add(Attributes.KNOCKBACK_RESISTANCE, 1f)
                        .add(Attributes.MAX_HEALTH, 5)
                        .add(Attributes.ARMOR, 8.0)
                .build());


    }

}
