package com.dpeter99.lob.book;

import com.dpeter99.lob.world.CellPos;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookAddressTest {
    @Test
    void zigzagRoundTripsAndIsNonNegative() {
        for (int n : new int[]{0, 1, -1, 2, -2, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            long z = BookAddress.zigzag(n);
            assertTrue(z >= 0, "zigzag(" + n + ") = " + z);
            assertEquals(n, BookAddress.unzigzag(z));
        }
    }

    @Test
    void cellNumberIsNonNegativeForNegativeCoordinates() {
        assertTrue(BookAddress.cellNumber(new CellPos(-5, -3, -9)).signum() >= 0);
        assertEquals(BigInteger.ZERO, BookAddress.cellNumber(new CellPos(0, 0, 0)));
    }

    @Test
    void cellNumberIsInjectiveOverGrid() {
        Set<BigInteger> seen = new HashSet<>();
        int n = 0;
        for (int x = -4; x <= 4; x++)
            for (int y = -4; y <= 4; y++)
                for (int z = -4; z <= 4; z++) {
                    seen.add(BookAddress.cellNumber(new CellPos(x, y, z)));
                    n++;
                }
        assertEquals(n, seen.size());
    }

    @Test
    void cellNumberRoundTrips() {
        CellPos[] cells = {
                new CellPos(0, 0, 0), new CellPos(1, 0, 0), new CellPos(0, -1, 0), new CellPos(-7, 3, 12),
                new CellPos(1000, -1000, 5), new CellPos(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)};
        for (CellPos cell : cells) {
            assertEquals(cell, BookAddress.cellFromNumber(BookAddress.cellNumber(cell)));
        }
    }

    @Test
    void bookNumberSeparatesIndicesAndCells() {
        BigInteger a = new BookAddress(null, new CellPos(1, 2, 3), 0).bookNumber(640);
        BigInteger b = new BookAddress(null, new CellPos(1, 2, 3), 1).bookNumber(640);
        BigInteger c = new BookAddress(null, new CellPos(1, 2, 4), 0).bookNumber(640);
        assertEquals(a.add(BigInteger.ONE), b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
        // consecutive indices in a cell never collide with another cell's range
        BigInteger last = new BookAddress(null, new CellPos(1, 2, 3), 639).bookNumber(640);
        assertEquals(BigInteger.ZERO, a.mod(BigInteger.valueOf(640)));
        assertEquals(a.divide(BigInteger.valueOf(640)), last.divide(BigInteger.valueOf(640)));
    }
}
