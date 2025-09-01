package net.sam.ping_system.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    //public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_DAMAGE_NUMBERS;

    static {
        BUILDER.push("Config for Sam's Combat Indicators");


        // DAMAGE NUMBER SETTINGS
        //ENABLE_DAMAGE_NUMBERS = BUILDER.comment("Should damage numbers show DEFAULT: true").define("enable_damage_numbers", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
