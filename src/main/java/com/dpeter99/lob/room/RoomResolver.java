package com.dpeter99.lob.room;

import com.dpeter99.lob.world.CellHash;
import com.dpeter99.lob.world.CellPos;
import net.minecraft.core.Vec3i;

import java.util.List;

/**
 * Decides which room occupies each cell, as a pure function of the world seed and the cell.
 * <p>
 * Every cell is a potential <em>anchor</em>: its hash picks one room by weight from the whole
 * room list and gives the anchor a priority. A room anchored at {@code a} covers the cells
 * {@code [a, a + size)}. An anchor is placed only if its priority beats every other anchor whose
 * footprint would intersect its own, so placed rooms never overlap and the decision for a cell
 * only depends on anchors within {@code maxSize} cells of it. Cells left uncovered by that rule
 * get a single-cell room picked by weight among the single-cell rooms.
 */
public final class RoomResolver {
    /** Upper bound on room size per axis, keeps the neighbourhood search small. */
    public static final int MAX_SIZE = 8;

    private static final long SALT_PICK = 0x524F4F4D5049434BL;
    private static final long SALT_PRIORITY = 0x524F4F4D50524921L;
    private static final long SALT_FILL = 0x524F4F4D46494C4CL;

    /** A room instance: which room and the cell holding its lowest corner. */
    public record Placement(RoomDefinition room, CellPos origin) {
        public boolean contains(CellPos cell) {
            Vec3i s = room.sizeInCells();
            return cell.x() >= origin.x() && cell.x() < origin.x() + s.getX()
                    && cell.y() >= origin.y() && cell.y() < origin.y() + s.getY()
                    && cell.z() >= origin.z() && cell.z() < origin.z() + s.getZ();
        }

        boolean intersects(Placement other) {
            Vec3i s = room.sizeInCells();
            Vec3i o = other.room.sizeInCells();
            return origin.x() < other.origin.x() + o.getX() && other.origin.x() < origin.x() + s.getX()
                    && origin.y() < other.origin.y() + o.getY() && other.origin.y() < origin.y() + s.getY()
                    && origin.z() < other.origin.z() + o.getZ() && other.origin.z() < origin.z() + s.getZ();
        }
    }

    private final List<RoomDefinition> rooms;
    private final int totalWeight;
    private final List<RoomDefinition> singleCellRooms;
    private final int singleCellWeight;
    private final int maxSize;

    public RoomResolver(List<RoomDefinition> rooms) {
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("A library needs at least one room");
        }
        int max = 1;
        for (RoomDefinition room : rooms) {
            Vec3i s = room.sizeInCells();
            if (s.getX() > MAX_SIZE || s.getY() > MAX_SIZE || s.getZ() > MAX_SIZE) {
                throw new IllegalArgumentException("Room " + room.template() + " is " + s + " cells; at most " + MAX_SIZE + " per axis");
            }
            max = Math.max(max, Math.max(s.getX(), Math.max(s.getY(), s.getZ())));
        }
        this.rooms = List.copyOf(rooms);
        this.totalWeight = rooms.stream().mapToInt(RoomDefinition::weight).sum();
        this.singleCellRooms = rooms.stream().filter(r -> !r.isLarge()).toList();
        this.singleCellWeight = singleCellRooms.stream().mapToInt(RoomDefinition::weight).sum();
        this.maxSize = max;
        if (singleCellRooms.isEmpty()) {
            throw new IllegalArgumentException("A library needs at least one 1x1x1 room to fill gaps between larger rooms");
        }
    }

    public Placement resolve(long seed, CellPos cell) {
        // Any anchor within maxSize-1 cells below/behind the cell could cover it.
        for (int dx = 0; dx < maxSize; dx++) {
            for (int dy = 0; dy < maxSize; dy++) {
                for (int dz = 0; dz < maxSize; dz++) {
                    CellPos anchor = cell.offset(-dx, -dy, -dz);
                    Placement candidate = anchored(seed, anchor);
                    if (candidate.contains(cell) && isPlaced(seed, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return new Placement(pick(singleCellRooms, singleCellWeight, CellHash.hash(seed, cell, SALT_FILL)), cell);
    }

    /** The room this anchor would place if nothing outranks it. */
    private Placement anchored(long seed, CellPos anchor) {
        return new Placement(pick(rooms, totalWeight, CellHash.hash(seed, anchor, SALT_PICK)), anchor);
    }

    private long priority(long seed, CellPos anchor) {
        return CellHash.hash(seed, anchor, SALT_PRIORITY);
    }

    /** True if no intersecting anchor has a higher priority. Ties are broken by anchor order. */
    private boolean isPlaced(long seed, Placement candidate) {
        long own = priority(seed, candidate.origin());
        Vec3i s = candidate.room().sizeInCells();
        CellPos o = candidate.origin();
        for (int x = o.x() - maxSize + 1; x < o.x() + s.getX(); x++) {
            for (int y = o.y() - maxSize + 1; y < o.y() + s.getY(); y++) {
                for (int z = o.z() - maxSize + 1; z < o.z() + s.getZ(); z++) {
                    CellPos other = new CellPos(x, y, z);
                    if (other.equals(o)) continue;
                    Placement rival = anchored(seed, other);
                    if (!rival.intersects(candidate)) continue;
                    long theirs = priority(seed, other);
                    if (theirs > own || (theirs == own && compare(other, o) < 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static int compare(CellPos a, CellPos b) {
        int c = Integer.compare(a.x(), b.x());
        if (c == 0) c = Integer.compare(a.y(), b.y());
        if (c == 0) c = Integer.compare(a.z(), b.z());
        return c;
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
