package com.finderfeed.raids_enhanced;

import com.finderfeed.fdlib.systems.config.ConfigValue;
import com.finderfeed.fdlib.systems.config.ReflectiveJsonConfig;

public class REConfig extends ReflectiveJsonConfig {

    @ConfigValue
    public float zapperStaffBallLightningDamage = 5f;

    @ConfigValue
    public float zapperStaffLightningDamage = 15f;

    @ConfigValue
    public int zapperStaffLightningUseCooldown = 60;

    @ConfigValue
    public int zapperStaffBallLightningUseCooldown = 20;

    @ConfigValue
    public float handcannonBombDamage = 10;

    @ConfigValue
    public int handcannonUseCooldown = 30;

    @ConfigValue
    public RaidDrillConfig raidDrill = new RaidDrillConfig();

    public REConfig() {
        super(RaidsEnhanced.location("raidsenhanced"));
    }

    @Override
    public boolean isClientside() {
        return false;
    }

    public static class RaidDrillConfig {

        @ConfigValue
        public int minRaidersSpawn = 2;

        @ConfigValue
        public int maxRaidersSpawn = 3;

    }

}
