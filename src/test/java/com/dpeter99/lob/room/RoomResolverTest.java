package com.dpeter99.lob.room;

import com.dpeter99.lob.world.CellPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomResolverTest {
    private static Identifier id(String s) {
        return Identifier.fromNamespaceAndPath("lob", s);
    }

    private static final RoomDefinition BASIC = RoomDefinition.small(id("basic"), 3);
    private static final RoomDefinition RARE = RoomDefinition.small(id("rare"), 1);
    private static final RoomDefinition HALL = new RoomDefinition(id("hall"), new Vec3i(3, 2, 3), 1, Optional.empty());
    private static final RoomDefinition TOWER = new RoomDefinition(id("tower"), new Vec3i(2, 4, 2), 1, Optional.empty());

    private static final RoomResolver RESOLVER = new RoomResolver(List.of(BASIC, RARE, HALL, TOWER), 6, 100);

    @Test
    void deterministic() {
        CellPos c = new CellPos(-13, 4, 77);
        assertEquals(RESOLVER.resolve(99L, c), RESOLVER.resolve(99L, c));
    }

    @Test
    void everyCellCoveredByLargeRoomAgreesOnPlacement() {
        int seen = 0;
        for (int x = -24; x < 24; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = -24; z < 24; z++) {
                    CellPos cell = new CellPos(x, y, z);
                    RoomResolver.Placement p = RESOLVER.resolve(1234L, cell);
                    assertTrue(p.contains(cell), "placement must cover the queried cell " + cell);
                    if (p.room().isLarge()) {
                        seen++;
                        Vec3i s = p.room().sizeInCells();
                        for (int dx = 0; dx < s.getX(); dx++)
                            for (int dy = 0; dy < s.getY(); dy++)
                                for (int dz = 0; dz < s.getZ(); dz++) {
                                    CellPos other = p.origin().offset(dx, dy, dz);
                                    assertEquals(p, RESOLVER.resolve(1234L, other), "cell " + other + " disagrees with " + cell);
                                }
                    } else {
                        assertEquals(cell, p.origin());
                    }
                }
            }
        }
        assertTrue(seen > 0, "expected some large rooms");
    }

    @Test
    void largeRoomsNeverOverlap() {
        Map<CellPos, RoomResolver.Placement> owner = new HashMap<>();
        for (int x = -30; x < 30; x++)
            for (int y = 0; y < 12; y++)
                for (int z = -30; z < 30; z++) {
                    RoomResolver.Placement p = RESOLVER.resolve(7L, new CellPos(x, y, z));
                    if (!p.room().isLarge()) continue;
                    RoomResolver.Placement prev = owner.put(new CellPos(x, y, z), p);
                    assertTrue(prev == null || prev.equals(p));
                }
    }

    @Test
    void zeroChanceMeansNoLargeRooms() {
        RoomResolver r = new RoomResolver(List.of(BASIC, HALL), 6, 0);
        for (int x = -20; x < 20; x++)
            for (int z = -20; z < 20; z++)
                assertFalse(r.resolve(5L, new CellPos(x, 0, z)).room().isLarge());
    }

    @Test
    void smallRoomWeightsAreRespected() {
        RoomResolver r = new RoomResolver(List.of(BASIC, RARE), 8, 0);
        int basic = 0, total = 0;
        for (int x = 0; x < 200; x++)
            for (int z = 0; z < 200; z++) {
                total++;
                if (r.resolve(42L, new CellPos(x, 0, z)).room() == BASIC) basic++;
            }
        double ratio = basic / (double) total;
        assertTrue(ratio > 0.72 && ratio < 0.78, "ratio " + ratio);
    }

    @Test
    void rejectsLargeRoomBiggerThanSpacing() {
        assertThrows(IllegalArgumentException.class, () -> new RoomResolver(List.of(BASIC, HALL), 2, 100));
    }

    @Test
    void rejectsLibraryWithoutSmallRooms() {
        assertThrows(IllegalArgumentException.class, () -> new RoomResolver(List.of(HALL), 8, 100));
    }
}
