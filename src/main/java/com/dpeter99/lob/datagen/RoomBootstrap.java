package com.dpeter99.lob.datagen;

import com.dpeter99.lob.LobMod;
import com.dpeter99.lob.registry.LobDimensions;
import com.dpeter99.lob.registry.LobRegistries;
import com.dpeter99.lob.room.LibraryDefinition;
import com.dpeter99.lob.room.RoomDefinition;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

/** The shipped rooms and libraries. This is the canonical list; JSON is generated from it. */
public final class RoomBootstrap {
    private RoomBootstrap() {
    }

    public static final ResourceKey<RoomDefinition> ROOM_BASIC = room("room_basic");
    public static final ResourceKey<RoomDefinition> ROOM_CROSSING = room("room_crossing");
    public static final ResourceKey<RoomDefinition> ROOM_STAIRWELL = room("room_stairwell");
    public static final ResourceKey<RoomDefinition> HALL_GRAND_TO_BETA = room("hall_grand_to_beta");
    public static final ResourceKey<RoomDefinition> HALL_GRAND_TO_ALPHA = room("hall_grand_to_alpha");
    public static final ResourceKey<RoomDefinition> HALL_EXIT = room("hall_exit");

    public static final ResourceKey<LibraryDefinition> LIBRARY_ALPHA = library("library_alpha");
    public static final ResourceKey<LibraryDefinition> LIBRARY_BETA = library("library_beta");

    private static ResourceKey<RoomDefinition> room(String name) {
        return ResourceKey.create(LobRegistries.ROOM, LobMod.id(name));
    }

    private static ResourceKey<LibraryDefinition> library(String name) {
        return ResourceKey.create(LobRegistries.LIBRARY, LobMod.id(name));
    }

    static void rooms(BootstrapContext<RoomDefinition> ctx) {
        ctx.register(ROOM_BASIC, RoomDefinition.small(LobMod.id("room_basic"), 6));
        ctx.register(ROOM_CROSSING, RoomDefinition.small(LobMod.id("room_crossing"), 3));
        ctx.register(ROOM_STAIRWELL, RoomDefinition.small(LobMod.id("room_stairwell"), 2));
        // The same grand hall template is used with two different portal targets.
        ctx.register(HALL_GRAND_TO_BETA, new RoomDefinition(LobMod.id("hall_grand"), new Vec3i(3, 2, 3), 3, Optional.of(LobDimensions.LIBRARY_BETA)));
        ctx.register(HALL_GRAND_TO_ALPHA, new RoomDefinition(LobMod.id("hall_grand"), new Vec3i(3, 2, 3), 3, Optional.of(LobDimensions.LIBRARY_ALPHA)));
        ctx.register(HALL_EXIT, new RoomDefinition(LobMod.id("hall_exit"), new Vec3i(2, 1, 2), 1, Optional.of(net.minecraft.world.level.Level.OVERWORLD)));
    }

    static void libraries(BootstrapContext<LibraryDefinition> ctx) {
        HolderGetter<RoomDefinition> rooms = ctx.lookup(LobRegistries.ROOM);
        ctx.register(LIBRARY_ALPHA, new LibraryDefinition(HolderSet.direct(
                rooms.getOrThrow(ROOM_BASIC), rooms.getOrThrow(ROOM_CROSSING), rooms.getOrThrow(ROOM_STAIRWELL),
                rooms.getOrThrow(HALL_GRAND_TO_BETA), rooms.getOrThrow(HALL_EXIT)), 8, 100));
        ctx.register(LIBRARY_BETA, new LibraryDefinition(HolderSet.direct(
                rooms.getOrThrow(ROOM_CROSSING), rooms.getOrThrow(ROOM_STAIRWELL),
                rooms.getOrThrow(HALL_GRAND_TO_ALPHA), rooms.getOrThrow(HALL_EXIT)), 6, 80));
    }
}
