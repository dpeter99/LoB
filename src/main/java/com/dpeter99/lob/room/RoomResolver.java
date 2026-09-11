package com.dpeter99.lob.room;

import com.dpeter99.lob.world.CellHash;
import com.dpeter99.lob.world.CellPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides which room occupies each cell, as a pure function of the world seed and the cell.
 * <p>
 * Large rooms live on a lattice of cubic "slots" of {@code spacing} cells. Each slot is hashed to
 * decide whether it holds a large room, which one, and where inside the slot it sits. Because every
 * large room fits inside its slot, a cell only needs to inspect its own slot. Cells not covered by a
 * large room get a weighted-random small room.
 */
public final class RoomResolver {
    private static final long SALT_SMALL = 0x534D414C4C524F4DL;
    private static final long SALT_LARGE_PRESENT = 0x4C41524745505245L;
    private static final long SALT_LARGE_PICK = 0x4C41524745504943L;
    private static final long SALT_LARGE_OFFSET = 0x4C4152474F464653L;

    /** A room instance: which room and the cell holding its lowest corner. */
    public record Placement(RoomDefinition room, CellPos origin) {
        public boolean contains(CellPos cell) {
            Vec3i s = room.sizeInCells();
            return cell.x() >= origin.x() && cell.x() < origin.x() + s.getX()
                    && cell.y() >= origin.y() && cell.y() < origin.y() + s.getY()
                    && cell.z() >= origin.z() && cell.z() < origin.z() + s.getZ();
        }
    }

    private final List<RoomDefinition> small;
    private final int smallTotalWeight;
    private final List<RoomDefinition> large;
    private final int largeTotalWeight;
    private final int spacing;
    private final int largeChancePercent;

    public RoomResolver(List<RoomDefinition> rooms, int spacing, int largeChancePercent) {
        if (spacing < 1) {
            throw new IllegalArgumentException("large_room_spacing must be at least 1");
        }
        List<RoomDefinition> small = new ArrayList<>();
        List<RoomDefinition> large = new ArrayList<>();
        for (RoomDefinition room : rooms) {
            if (room.isLarge()) {
                Vec3i s = room.sizeInCells();
                if (s.getX() > spacing || s.getY() > spacing || s.getZ() > spacing) {
                    throw new IllegalArgumentException("Large room " + room.template() + " is " + s
                            + " cells but must fit inside the large_room_spacing of " + spacing);
                }
                large.add(room);
            } else {
                small.add(room);
            }
        }
        if (small.isEmpty()) {
            throw new IllegalArgumentException("A library needs at least one 1x1x1 room");
        }
        this.small = List.copyOf(small);
        this.large = List.copyOf(large);
        this.smallTotalWeight = small.stream().mapToInt(RoomDefinition::weight).sum();
        this.largeTotalWeight = large.stream().mapToInt(RoomDefinition::weight).sum();
        this.spacing = spacing;
        this.largeChancePercent = largeChancePercent;
    }

    public Placement resolve(long seed, CellPos cell) {
        Placement largePlacement = largeRoomInSlot(seed, slotOf(cell));
        if (largePlacement != null && largePlacement.contains(cell)) {
            return largePlacement;
        }
        RoomDefinition room = pick(small, smallTotalWeight, CellHash.hash(seed, cell, SALT_SMALL));
        return new Placement(room, cell);
    }

    private CellPos slotOf(CellPos cell) {
        return new CellPos(Math.floorDiv(cell.x(), spacing), Math.floorDiv(cell.y(), spacing), Math.floorDiv(cell.z(), spacing));
    }

    /** The large room placed in a lattice slot, or null if the slot has none. */
    private Placement largeRoomInSlot(long seed, CellPos slot) {
        if (large.isEmpty() || largeChancePercent <= 0) {
            return null;
        }
        if (CellHash.bounded(CellHash.hash(seed, slot, SALT_LARGE_PRESENT), 100) >= largeChancePercent) {
            return null;
        }
        RoomDefinition room = pick(large, largeTotalWeight, CellHash.hash(seed, slot, SALT_LARGE_PICK));
        long offsetHash = CellHash.hash(seed, slot, SALT_LARGE_OFFSET);
        Vec3i s = room.sizeInCells();
        int ox = CellHash.bounded(offsetHash, spacing - s.getX() + 1);
        int oy = CellHash.bounded(offsetHash >>> 21, spacing - s.getY() + 1);
        int oz = CellHash.bounded(offsetHash >>> 42, spacing - s.getZ() + 1);
        CellPos origin = new CellPos(slot.x() * spacing + ox, slot.y() * spacing + oy, slot.z() * spacing + oz);
        return new Placement(room, origin);
    }

    private static RoomDefinition pick(List<RoomDefinition> rooms, int totalWeight, long hash) {
        int roll = CellHash.bounded(hash, totalWeight);
        for (RoomDefinition room : rooms) {
            roll -= room.weight();
            if (roll < 0) {
                return room;
            }
        }
        return rooms.getLast();
    }
}
