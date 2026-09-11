package com.dpeter99.lob.world;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellHashTest {
    @Test
    void deterministic() {
        assertEquals(CellHash.hash(42L, 1, 2, 3, 7), CellHash.hash(42L, 1, 2, 3, 7));
    }

    @Test
    void inputsMatter() {
        long base = CellHash.hash(42L, 1, 2, 3, 7);
        assertNotEquals(base, CellHash.hash(43L, 1, 2, 3, 7));
        assertNotEquals(base, CellHash.hash(42L, 2, 2, 3, 7));
        assertNotEquals(base, CellHash.hash(42L, 1, 3, 3, 7));
        assertNotEquals(base, CellHash.hash(42L, 1, 2, 4, 7));
        assertNotEquals(base, CellHash.hash(42L, 1, 2, 3, 8));
        assertNotEquals(CellHash.hash(0L, 0, 0, 0, 0), CellHash.hash(0L, 0, 0, 1, 0));
    }

    @Test
    void fewCollisionsOverGrid() {
        Set<Long> seen = new HashSet<>();
        int n = 0;
        for (int x = -20; x < 20; x++)
            for (int y = 0; y < 16; y++)
                for (int z = -20; z < 20; z++) {
                    seen.add(CellHash.hash(1234L, x, y, z, 1));
                    n++;
                }
        assertTrue(seen.size() > n * 0.999, "collisions: " + (n - seen.size()));
    }

    @Test
    void boundedIsUniformish() {
        int[] buckets = new int[10];
        for (int i = 0; i < 100_000; i++) {
            buckets[CellHash.bounded(CellHash.hash(9L, i, 0, 0, 3), 10)]++;
        }
        for (int b : buckets) {
            assertTrue(b > 9_000 && b < 11_000, "bucket " + b);
        }
    }
}
