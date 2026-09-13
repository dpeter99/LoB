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

    public static final ModConfigSpec.IntValue BOOK_PAGES = BUILDER
            .comment("Pages in a library book. The vanilla book screen shows about 14 lines of about 19 characters per page.")
            .defineInRange("bookPages", 8, 1, 100);

    public static final ModConfigSpec.IntValue BOOK_LINES_PER_PAGE = BUILDER
            .comment("Lines of text on each page of a library book.")
            .defineInRange("bookLinesPerPage", 14, 1, 14);

    public static final ModConfigSpec.IntValue BOOK_CHARS_PER_LINE = BUILDER
            .comment("Characters on each line of a library book.")
            .defineInRange("bookCharsPerLine", 19, 1, 64);

    public static final ModConfigSpec.IntValue BOOKS_PER_CELL = BUILDER
            .comment("How many distinct books each 16x16x16 library cell holds.")
            .defineInRange("booksPerCell", 640, 1, 1_000_000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private LobConfig() {
    }

    private static boolean isValidIdentifier(Object value) {
        return value instanceof String s && Identifier.tryParse(s) != null;
    }
}
