package com.dpeter99.lob.room;

import com.dpeter99.lob.registry.LobRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFixedCodec;

/**
 * The room mix of one library dimension, loaded from {@code data/<ns>/lob/library/*.json}.
 *
 * @param rooms                 rooms that may appear; small and large rooms are separated automatically
 * @param largeRoomSpacing      lattice period in cells for large rooms; every large room must fit inside one lattice slot
 * @param largeRoomChancePercent chance that a lattice slot contains a large room
 */
public record LibraryDefinition(HolderSet<RoomDefinition> rooms, int largeRoomSpacing, int largeRoomChancePercent) {
    public static final Codec<LibraryDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(LobRegistries.ROOM).fieldOf("rooms").forGetter(LibraryDefinition::rooms),
            Codec.intRange(1, 64).optionalFieldOf("large_room_spacing", 8).forGetter(LibraryDefinition::largeRoomSpacing),
            Codec.intRange(0, 100).optionalFieldOf("large_room_chance", 100).forGetter(LibraryDefinition::largeRoomChancePercent)
    ).apply(i, LibraryDefinition::new));

    public static final Codec<Holder<LibraryDefinition>> HOLDER_CODEC = RegistryFixedCodec.create(LobRegistries.LIBRARY);

    /** Builds the immutable resolver for this library; throws with a clear message on invalid room sizes. */
    public RoomResolver createResolver() {
        return new RoomResolver(rooms.stream().map(Holder::value).toList(), largeRoomSpacing, largeRoomChancePercent);
    }
}
