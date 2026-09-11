package com.dpeter99.borgeslib;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A keyed bijection on the integers {@code [0, size)}, the backbone of Library of Babel style
 * addressing: every address maps to exactly one book and every book has exactly one address.
 * <p>
 * Contract:
 * <ul>
 *   <li>every input in range maps to exactly one output in range, and every output is produced once</li>
 *   <li>the mapping is deterministic for a given seed</li>
 *   <li>{@link #decode} inverts {@link #encode}</li>
 *   <li>neighbouring inputs land far apart so consecutive addresses look unrelated</li>
 * </ul>
 * The current implementation is an affine permutation {@code (a * x + b) mod size} with
 * {@code gcd(a, size) == 1}. It satisfies the contract; a Feistel-style permutation would make
 * outputs look more random and can replace it without changing the API.
 */
public final class EncodingLib {
    private final BigInteger size;
    private final BigInteger multiplier;
    private final BigInteger inverseMultiplier;
    private final BigInteger offset;

    public EncodingLib(long seed, BigInteger size) {
        if (size.signum() <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.size = size;
        this.multiplier = deriveMultiplier(seed, size);
        this.inverseMultiplier = size.equals(BigInteger.ONE) ? BigInteger.ZERO : multiplier.modInverse(size);
        this.offset = new BigInteger(Long.toUnsignedString(mix(seed ^ 0x5DEECE66DL))).mod(size);
    }

    public BigInteger size() {
        return size;
    }

    public BigInteger encode(BigInteger index) {
        checkRange(index);
        return index.multiply(multiplier).add(offset).mod(size);
    }

    public BigInteger decode(BigInteger encoded) {
        checkRange(encoded);
        return encoded.subtract(offset).multiply(inverseMultiplier).mod(size);
    }

    private void checkRange(BigInteger value) {
        Objects.requireNonNull(value);
        if (value.signum() < 0 || value.compareTo(size) >= 0) {
            throw new IllegalArgumentException(value + " is outside [0, " + size + ")");
        }
    }

    /** Picks a large odd multiplier from the seed and nudges it until it is coprime with size. */
    private static BigInteger deriveMultiplier(long seed, BigInteger size) {
        if (size.equals(BigInteger.ONE)) {
            return BigInteger.ONE;
        }
        // spread the multiplier across the whole range so adjacent inputs jump far apart
        BigInteger candidate = new BigInteger(Long.toUnsignedString(mix(seed))).mod(size);
        if (candidate.compareTo(BigInteger.TWO) < 0) {
            candidate = size.divide(BigInteger.TWO).max(BigInteger.TWO);
        }
        while (!candidate.gcd(size).equals(BigInteger.ONE)) {
            candidate = candidate.add(BigInteger.ONE);
            if (candidate.compareTo(size) >= 0) {
                candidate = BigInteger.TWO;
            }
        }
        return candidate;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
