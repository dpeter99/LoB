package com.dpeter99.lob.room;

import com.dpeter99.lob.world.CellPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * One kind of library room, loaded from {@code data/<ns>/lob/library_room/*.json}.
 *
 * @param template     structure template whose size must be {@code sizeInCells * 16} on every axis
 * @param sizeInCells  footprint in 16-block cells; anything other than 1x1x1 is a "large" room
 * @param weight       relative pick weight among rooms of the same class (small or large)
 * @param portalTarget dimension that portal blocks inside this room lead to, if any
 */
public record RoomDefinition(Identifier template, Vec3i sizeInCells, int weight, Optional<ResourceKey<Level>> portalTarget) {
    public static final Vec3i ONE_CELL = new Vec3i(1, 1, 1);

    public static final Codec<RoomDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("template").forGetter(RoomDefinition::template),
            Vec3i.CODEC.optionalFieldOf("size_in_cells", ONE_CELL).forGetter(RoomDefinition::sizeInCells),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("weight", 1).forGetter(RoomDefinition::weight),
            ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("portal_target").forGetter(RoomDefinition::portalTarget)
    ).apply(i, RoomDefinition::new));

    public RoomDefinition {
        if (sizeInCells.getX() < 1 || sizeInCells.getY() < 1 || sizeInCells.getZ() < 1) {
            throw new IllegalArgumentException("Room " + template + " size_in_cells must be at least 1 on every axis");
        }
    }

    public static RoomDefinition small(Identifier template, int weight) {
        return new RoomDefinition(template, ONE_CELL, weight, Optional.empty());
    }

    public boolean isLarge() {
        return !ONE_CELL.equals(sizeInCells);
    }

    /** Template size in blocks. */
    public Vec3i templateSize() {
        return new Vec3i(sizeInCells.getX() * CellPos.SIZE, sizeInCells.getY() * CellPos.SIZE, sizeInCells.getZ() * CellPos.SIZE);
    }
}
