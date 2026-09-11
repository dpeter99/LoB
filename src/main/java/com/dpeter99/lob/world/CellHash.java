package com.dpeter99.lob.world;

import net.minecraft.util.RandomSource;

/**
 * Deterministic hashing of cell coordinates. Everything about which room sits in a cell
 * derives from {@link #hash}, so any chunk can be generated independently and agree
 * with its neighbours.
 */
public final class CellHash {
    private CellHash() {
    }

    /** SplitMix64-style mix of seed, coordinates and a purpose salt. */
    public static long hash(long seed, int x, int y, int z, long salt) {
        long h = seed ^ 0x9E3779B97F4A7C15L;
        h = mix(h + salt);
        h = mix(h ^ (x * 0xBF58476D1CE4E5B9L));
        h = mix(h ^ (y * 0x94D049BB133111EBL));
        h = mix(h ^ (z * 0xD6E8FEB86659FD93L));
        return h;
    }

    public static long hash(long seed, CellPos cell, long salt) {
        return hash(seed, cell.x(), cell.y(), cell.z(), salt);
    }

    /** A random source seeded from the cell hash; cheap to create per cell. */
    public static RandomSource random(long seed, CellPos cell, long salt) {
        return RandomSource.create(hash(seed, cell, salt));
    }

    /** Maps a hash to a uniform int in [0, bound). */
    public static int bounded(long hash, int bound) {
        return (int) Long.remainderUnsigned(hash, bound);
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
