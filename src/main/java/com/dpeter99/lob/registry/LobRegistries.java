package com.dpeter99.lob.registry;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.room.LibraryDefinition;
import com.dpeter99.lob.room.RoomDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/** Datapack registries: rooms and libraries are plain JSON that any datapack can extend. */
@EventBusSubscriber(modid = LobMod.MOD_ID)
public final class LobRegistries {
    private LobRegistries() {
    }

    public static final ResourceKey<Registry<RoomDefinition>> ROOM = ResourceKey.createRegistryKey(LobMod.id("library_room"));
    public static final ResourceKey<Registry<LibraryDefinition>> LIBRARY = ResourceKey.createRegistryKey(LobMod.id("library"));

    @SubscribeEvent
    static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ROOM, RoomDefinition.CODEC);
        event.dataPackRegistry(LIBRARY, LibraryDefinition.CODEC);
    }
}
