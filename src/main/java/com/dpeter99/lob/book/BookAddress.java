package com.dpeter99.lob.book;

import com.dpeter99.lob.world.CellPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.math.BigInteger;

/**
 * Where a book was taken from: a library dimension, the cell inside it and the book's index
 * within that cell. Stored on the item as the {@code lob:book_address} data component.
 * <p>
 * The static math below turns an address into a single non-negative number and back. It is
 * registry-free so it can be unit tested; the codecs are the only part that touches Minecraft.
 * The dimension is stored but does not enter the number yet: once shuffling is wired it will
 * seed the {@link com.dpeter99.borgeslib.EncodingLib} permutation instead.
 */
public record BookAddress(ResourceKey<Level> dimension, CellPos cell, int index) {
    private static final Codec<CellPos> CELL_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("x").forGetter(CellPos::x),
            Codec.INT.fieldOf("y").forGetter(CellPos::y),
            Codec.INT.fieldOf("z").forGetter(CellPos::z)
    ).apply(i, CellPos::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CellPos> CELL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CellPos::x,
            ByteBufCodecs.VAR_INT, CellPos::y,
            ByteBufCodecs.VAR_INT, CellPos::z,
            CellPos::new);

    public static final Codec<BookAddress> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(BookAddress::dimension),
            CELL_CODEC.fieldOf("cell").forGetter(BookAddress::cell),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("index").forGetter(BookAddress::index)
    ).apply(i, BookAddress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BookAddress> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), BookAddress::dimension,
            CELL_STREAM_CODEC, BookAddress::cell,
            ByteBufCodecs.VAR_INT, BookAddress::index,
            BookAddress::new);

    public BookAddress {
        if (index < 0) {
            throw new IllegalArgumentException("book index must not be negative: " + index);
        }
    }

    /** The number this address denotes: every cell gets {@code booksPerCell} consecutive numbers. */
    public BigInteger bookNumber(int booksPerCell) {
        if (booksPerCell <= 0) {
            throw new IllegalArgumentException("booksPerCell must be positive");
        }
        return cellNumber(cell).multiply(BigInteger.valueOf(booksPerCell)).add(BigInteger.valueOf(index));
    }

    /**
     * Bijection from cells (with possibly negative coordinates) to the non-negative integers:
     * each axis is zigzag-encoded to an unsigned 32-bit value, then the three values are
     * combined with the Szudzik pairing function twice.
     */
    public static BigInteger cellNumber(CellPos cell) {
        BigInteger zx = BigInteger.valueOf(zigzag(cell.x()));
        BigInteger zy = BigInteger.valueOf(zigzag(cell.y()));
        BigInteger zz = BigInteger.valueOf(zigzag(cell.z()));
        return pair(pair(zx, zy), zz);
    }

    /** Inverse of {@link #cellNumber}. */
    public static CellPos cellFromNumber(BigInteger number) {
        BigInteger[] outer = unpair(number);
        BigInteger[] inner = unpair(outer[0]);
        return new CellPos(unzigzag(inner[0].longValueExact()), unzigzag(inner[1].longValueExact()),
                unzigzag(outer[1].longValueExact()));
    }

    static long zigzag(int n) {
        return Integer.toUnsignedLong((n << 1) ^ (n >> 31));
    }

    static int unzigzag(long z) {
        int u = (int) z;
        return (u >>> 1) ^ -(u & 1);
    }

    /** Szudzik pairing: a bijection N x N -> N that keeps small inputs small. */
    static BigInteger pair(BigInteger a, BigInteger b) {
        return a.compareTo(b) >= 0
                ? a.multiply(a).add(a).add(b)
                : b.multiply(b).add(a);
    }

    static BigInteger[] unpair(BigInteger n) {
        BigInteger root = n.sqrt();
        BigInteger rootSquared = root.multiply(root);
        BigInteger rest = n.subtract(rootSquared);
        return rest.compareTo(root) < 0
                ? new BigInteger[]{rest, root}
                : new BigInteger[]{root, rest.subtract(root)};
    }
}
