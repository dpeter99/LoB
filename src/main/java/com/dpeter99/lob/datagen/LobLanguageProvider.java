package com.dpeter99.lob.datagen;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.registry.ModRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class LobLanguageProvider extends LanguageProvider {
    LobLanguageProvider(PackOutput output) {
        super(output, LobMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModRegistries.LIBRARY_PORTAL.get(), "Library Portal");
        add(ModRegistries.LIBRARIAN_KEY.get(), "Librarian's Key");
        add("item.lob.librarian_key.missing_dimension", "The library %s does not exist");
        add(ModRegistries.LIBRARY_BOOK.get(), "Library Book");
        add("item.lob.library_book.address", "%s, cell %s %s %s, book %s");
        add("command.lob.book.given", "Gave book %s cell %s %s %s index %s");
        add("command.lob.book.not_in_library", "You are not in a library; pass a cell explicitly");
        add("command.lob.book.index_out_of_range", "Book index %s is not below booksPerCell (%s)");
        add("lob.configuration.bookPages", "Book pages");
        add("lob.configuration.bookLinesPerPage", "Book lines per page");
        add("lob.configuration.bookCharsPerLine", "Book characters per line");
        add("lob.configuration.booksPerCell", "Books per cell");
        add("lob.configuration.title", "Library of Babel Configuration");
        add("lob.configuration.defaultEntryDimension", "Default entry dimension");
        add("lob.configuration.portalCooldownTicks", "Portal cooldown (ticks)");
        add("lob.configuration.debugLogging", "Debug logging");
    }
}
