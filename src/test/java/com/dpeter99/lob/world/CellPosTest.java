package com.dpeter99.lob.world;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellPosTest {
    @Test
    void blockToCellRoundTripsIncludingNegatives() {
        assertEquals(new CellPos(0, 0, 0), CellPos.fromBlock(0, 0, 0));
        assertEquals(new CellPos(0, 0, 0), CellPos.fromBlock(15, 15, 15));
        assertEquals(new CellPos(1, 1, 1), CellPos.fromBlock(16, 16, 16));
        assertEquals(new CellPos(-1, -1, -1), CellPos.fromBlock(-1, -1, -1));
        assertEquals(new CellPos(-1, -1, -1), CellPos.fromBlock(-16, -16, -16));
        assertEquals(new CellPos(-2, -2, -2), CellPos.fromBlock(-17, -17, -17));
    }

    @Test
    void originIsLowestCorner() {
        assertEquals(new BlockPos(-32, 48, 160), new CellPos(-2, 3, 10).origin());
        for (int b = -100; b <= 100; b++) {
            CellPos c = CellPos.fromBlock(b, b, b);
            BlockPos o = c.origin();
            assertEquals(c, CellPos.fromBlock(o));
            assertEquals(c, CellPos.fromBlock(o.offset(15, 15, 15)));
        }
    }
}
