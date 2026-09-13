package com.dpeter99.borgeslib;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncodingLibTest {
    private static void assertBijection(long seed, int size) {
        EncodingLib lib = new EncodingLib(seed, BigInteger.valueOf(size));
        BitSet seen = new BitSet(size);
        for (int i = 0; i < size; i++) {
            BigInteger in = BigInteger.valueOf(i);
            BigInteger out = lib.encode(in);
            assertTrue(out.signum() >= 0 && out.intValueExact() < size, "out of range: " + out);
            assertTrue(!seen.get(out.intValueExact()), "duplicate output " + out + " for input " + i);
            seen.set(out.intValueExact());
            assertEquals(in, lib.decode(out));
        }
        assertEquals(size, seen.cardinality());
    }


    void bijectionOverSmallRanges() {
        for (int size = 1; size <= 100; size++) {
            assertBijection(size * 31L, size);
        }
    }

    @Test
    void bijectionOverPowerOfTwoAndPrime() {
        assertBijection(9L, 1 << 16);
        assertBijection(10L, 65_521);
    }

    @Test
    void decodeInvertsEncodeForHugeSizes() {
        BigInteger size = BigInteger.TWO.pow(256);
        EncodingLib lib = new EncodingLib(123L, size);
        BigInteger x = new BigInteger("123456789012345678901234567890");
        assertEquals(x, lib.decode(lib.encode(x)));
        assertEquals(BigInteger.ZERO, lib.decode(lib.encode(BigInteger.ZERO)));
        assertEquals(size.subtract(BigInteger.ONE), lib.decode(lib.encode(size.subtract(BigInteger.ONE))));
    }

    @Test
    void differentSeedsDiffer() {
        EncodingLib a = new EncodingLib(1L, BigInteger.valueOf(1_000_003));
        EncodingLib b = new EncodingLib(2L, BigInteger.valueOf(1_000_003));
        assertNotEquals(a.encode(BigInteger.valueOf(17)), b.encode(BigInteger.valueOf(17)));
    }

    @Test
    void neighboursLandFarApart() {
        EncodingLib lib = new EncodingLib(5L, BigInteger.valueOf(1_000_000));
        BigInteger d = lib.encode(BigInteger.valueOf(1001)).subtract(lib.encode(BigInteger.valueOf(1000))).abs();
        assertTrue(d.intValue() > 1000, "distance " + d);
    }

    @Test
    void rejectsOutOfRange() {
        EncodingLib lib = new EncodingLib(5L, BigInteger.TEN);
        assertThrows(IllegalArgumentException.class, () -> lib.encode(BigInteger.TEN));
        assertThrows(IllegalArgumentException.class, () -> lib.decode(BigInteger.valueOf(-1)));
    }
}
