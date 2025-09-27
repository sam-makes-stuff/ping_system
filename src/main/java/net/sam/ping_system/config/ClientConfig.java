package net.sam.ping_system.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Float> PING_SCALE_MULT;
    public static final ForgeConfigSpec.ConfigValue<Float> PING_EDGE_PIXELS;

    static {
        BUILDER.push("Config for Ping System");

        PING_SCALE_MULT = BUILDER.comment("Scale multiplier for pings (pings also still scale with gui scale) DEFAULT: 1.0").define("ping_scale_mult", 1.0f);
        PING_EDGE_PIXELS = BUILDER.comment("Ping distance from screen border when locked to edge DEFAULT: 128.0").define("ping_edge_pixels", 128.0f);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

}
