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
        add("lob.configuration.title", "Library of Babel Configuration");
        add("lob.configuration.defaultEntryDimension", "Default entry dimension");
        add("lob.configuration.portalCooldownTicks", "Portal cooldown (ticks)");
        add("lob.configuration.debugLogging", "Debug logging");
    }
}
