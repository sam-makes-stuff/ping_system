package net.sam.ping_system.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_PINGS;
    public static final ForgeConfigSpec.ConfigValue<Double> PING_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TRACK_MOBS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TRACK_PLAYERS;

    static {
        BUILDER.push("Config for Ping System");

        MAX_PINGS = BUILDER.comment("Maximum pings per player DEFAULT: 3").define("max_pings", 3);
        PING_COOLDOWN = BUILDER.comment("Minimum time between pings (in ticks) DEFAULT: 12").define("ping_cooldown", 12.0);

        TRACK_MOBS = BUILDER.comment("Should pings follow mobs DEFAULT: true").define("track_mobs", true);
        TRACK_PLAYERS = BUILDER.comment("Should pings follow players DEFAULT: false").define("track_players", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
