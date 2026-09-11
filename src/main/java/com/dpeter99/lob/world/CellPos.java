package com.dpeter99.lob.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

/**
 * Position of one 16x16x16 library cell. Cell (x, y, z) covers blocks
 * [16x, 16x+15] x [16y, 16y+15] x [16z, 16z+15]. Horizontally a cell is a chunk;
 * vertically there are height/16 cells per column.
 */
public record CellPos(int x, int y, int z) {
    public static final int SIZE = 16;
    public static final int SHIFT = 4;

    public static CellPos fromBlock(int blockX, int blockY, int blockZ) {
        return new CellPos(blockX >> SHIFT, blockY >> SHIFT, blockZ >> SHIFT);
    }

    public static CellPos fromBlock(BlockPos pos) {
        return fromBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static CellPos of(ChunkPos chunk, int cellY) {
        return new CellPos(chunk.x(), cellY, chunk.z());
    }

    /** Lowest block corner of this cell. */
    public BlockPos origin() {
        return new BlockPos(x << SHIFT, y << SHIFT, z << SHIFT);
    }

    public int minBlockX() {
        return x << SHIFT;
    }

    public int minBlockY() {
        return y << SHIFT;
    }

    public int minBlockZ() {
        return z << SHIFT;
    }

    public CellPos offset(int dx, int dy, int dz) {
        return new CellPos(x + dx, y + dy, z + dz);
    }
}
