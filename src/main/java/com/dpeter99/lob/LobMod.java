package com.dpeter99.lob;

import com.dpeter99.lob.registry.ModRegistries;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entry point of the Library of Babel mod.
 */
@Mod(LobMod.MOD_ID)
public final class LobMod {
    public static final String MOD_ID = "lob";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LobMod(IEventBus modBus, ModContainer container) {
        ModRegistries.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, LobConfig.SPEC);
        LOGGER.info("Library of Babel loaded");
    }

    /** Creates an identifier in this mod's namespace. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
