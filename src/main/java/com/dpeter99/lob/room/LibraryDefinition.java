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
 * @param rooms rooms that may appear; each room brings its own size and weight
 */
public record LibraryDefinition(HolderSet<RoomDefinition> rooms) {
    public static final Codec<LibraryDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(LobRegistries.ROOM).fieldOf("rooms").forGetter(LibraryDefinition::rooms)
    ).apply(i, LibraryDefinition::new));

    public static final Codec<Holder<LibraryDefinition>> HOLDER_CODEC = RegistryFixedCodec.create(LobRegistries.LIBRARY);

    /** Builds the immutable resolver for this library; throws with a clear message on invalid rooms. */
    public RoomResolver createResolver() {
        return new RoomResolver(rooms.stream().map(Holder::value).toList());
    }
}
