package com.finderfeed.raids_enhanced.init;

import com.finderfeed.raids_enhanced.RaidsEnhanced;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimp;
import com.finderfeed.raids_enhanced.content.entities.raid_blimp.RaidBlimpCannonProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = RaidsEnhanced.MOD_ID)
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


    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(RAID_BLIMP.get(), RaidBlimp.createMonsterAttributes()
                        .add(Attributes.FOLLOW_RANGE, 40.0)
                        .add(Attributes.FLYING_SPEED, 0.3f)
                .build());
    }

}
