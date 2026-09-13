package com.dpeter99.lob.book;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a book number into page text. The number is written in the alphabet's radix, right
 * aligned across the whole book and padded on the left with digit 0 (the space), then cut into
 * pages of {@code linesPerPage} lines of {@code charsPerLine} characters.
 * <p>
 * Pure and deterministic: the same number and layout always give the same pages. No shuffling
 * is applied, so consecutive books read almost identically.
 */
public final class BookText {
    private BookText() {
    }

    /** Shape of a book. Later this becomes fully configurable per library. */
    public record BookLayout(int pages, int linesPerPage, int charsPerLine) {
        public BookLayout {
            if (pages <= 0 || linesPerPage <= 0 || charsPerLine <= 0) {
                throw new IllegalArgumentException("layout dimensions must be positive");
            }
        }

        public int charsPerPage() {
            return linesPerPage * charsPerLine;
        }

        public int totalChars() {
            return pages * charsPerPage();
        }

        /** Number of distinct books this layout can hold in the given alphabet. */
        public BigInteger capacity(BookAlphabet alphabet) {
            return BigInteger.valueOf(alphabet.radix()).pow(totalChars());
        }
    }

    /**
     * Pages for a book number. Numbers at or above the layout's capacity wrap modulo the
     * capacity so the output shape never depends on the input.
     */
    public static List<String> pages(BigInteger bookNumber, BookLayout layout, BookAlphabet alphabet) {
        if (bookNumber.signum() < 0) {
            throw new IllegalArgumentException("book number must not be negative");
        }
        char[] text = digits(bookNumber.mod(layout.capacity(alphabet)), layout.totalChars(), alphabet);
        List<String> pages = new ArrayList<>(layout.pages());
        for (int page = 0; page < layout.pages(); page++) {
            StringBuilder sb = new StringBuilder(layout.charsPerPage() + layout.linesPerPage());
            for (int line = 0; line < layout.linesPerPage(); line++) {
                if (line > 0) {
                    sb.append('\n');
                }
                int start = page * layout.charsPerPage() + line * layout.charsPerLine();
                sb.append(text, start, layout.charsPerLine());
            }
            pages.add(sb.toString());
        }
        return pages;
    }

    /** Inverse of {@link #pages}: reads the digits back into the number they encode. */
    public static BigInteger number(List<String> pages, BookAlphabet alphabet) {
        BigInteger radix = BigInteger.valueOf(alphabet.radix());
        BigInteger n = BigInteger.ZERO;
        for (String page : pages) {
            for (int i = 0; i < page.length(); i++) {
                char c = page.charAt(i);
                if (c == '\n') {
                    continue;
                }
                int digit = alphabet.digit(c);
                if (digit < 0) {
                    throw new IllegalArgumentException("'" + c + "' is not in the alphabet");
                }
                n = n.multiply(radix).add(BigInteger.valueOf(digit));
            }
        }
        return n;
    }

    /** Fixed-width base-radix digits of {@code number}, most significant first, left padded with digit 0. */
    private static char[] digits(BigInteger number, int width, BookAlphabet alphabet) {
        char[] out = new char[width];
        java.util.Arrays.fill(out, alphabet.symbol(0));
        BigInteger radix = BigInteger.valueOf(alphabet.radix());
        int i = width - 1;
        while (number.signum() > 0 && i >= 0) {
            BigInteger[] qr = number.divideAndRemainder(radix);
            out[i--] = alphabet.symbol(qr[1].intValue());
            number = qr[0];
        }
        return out;
    }
}
