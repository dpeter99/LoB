package com.dpeter99.lob;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config. Variety of the libraries themselves lives in datapack JSON, not here.
 */
public final class LobConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_ENTRY_DIMENSION = BUILDER
            .comment("Dimension the Librarian's Key sends players to from outside a library.")
            .define("defaultEntryDimension", "lob:library_alpha", LobConfig::isValidIdentifier);

    public static final ModConfigSpec.IntValue PORTAL_COOLDOWN_TICKS = BUILDER
            .comment("Ticks an entity must wait between portal teleports.")
            .defineInRange("portalCooldownTicks", 40, 0, 20 * 60);

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Log room placement decisions and enable debug commands.")
            .define("debugLogging", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private LobConfig() {
    }

    private static boolean isValidIdentifier(Object value) {
        return value instanceof String s && Identifier.tryParse(s) != null;
    }
}
