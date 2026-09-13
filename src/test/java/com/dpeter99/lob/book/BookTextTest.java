package com.dpeter99.lob.book;

import com.dpeter99.lob.book.BookText.BookLayout;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTextTest {
    private static final BookAlphabet ALPHABET = BookAlphabet.BORGES;
    private static final BookLayout LAYOUT = new BookLayout(3, 4, 5);

    @Test
    void alphabetHasTwentyFiveSymbolsWithSpaceAsZero() {
        assertEquals(25, ALPHABET.radix());
        assertEquals(' ', ALPHABET.symbol(0));
        assertEquals('a', ALPHABET.symbol(1));
        assertEquals('.', ALPHABET.symbol(24));
        assertEquals(7, ALPHABET.digit('g'));
        assertEquals(-1, ALPHABET.digit('j'));
    }

    @Test
    void deterministic() {
        BigInteger n = BigInteger.valueOf(123456789L);
        assertEquals(BookText.pages(n, LAYOUT, ALPHABET), BookText.pages(n, LAYOUT, ALPHABET));
    }

    @Test
    void layoutShapeIsHonoured() {
        List<String> pages = BookText.pages(BigInteger.valueOf(98765), LAYOUT, ALPHABET);
        assertEquals(LAYOUT.pages(), pages.size());
        for (String page : pages) {
            String[] lines = page.split("\n", -1);
            assertEquals(LAYOUT.linesPerPage(), lines.length);
            for (String line : lines) {
                assertEquals(LAYOUT.charsPerLine(), line.length());
            }
        }
    }

    @Test
    void zeroIsAllBlank() {
        for (String page : BookText.pages(BigInteger.ZERO, LAYOUT, ALPHABET)) {
            assertTrue(page.replace("\n", "").isBlank());
        }
    }

    @Test
    void smallNumbersAreRightAligned() {
        List<String> pages = BookText.pages(BigInteger.ONE, LAYOUT, ALPHABET);
        String last = pages.get(pages.size() - 1);
        assertEquals('a', last.charAt(last.length() - 1));
        assertTrue(last.substring(0, last.length() - 1).replace("\n", "").isBlank());
        assertTrue(pages.get(0).replace("\n", "").isBlank());
    }

    @Test
    void baseConversion() {
        // 25 = "a" followed by digit 0 -> "a "
        String last = lastPage(BigInteger.valueOf(25));
        assertEquals("a ", last.substring(last.length() - 2));
        // 25*25 + 2*25 + 24 = "ab."
        last = lastPage(BigInteger.valueOf(25 * 25 + 2 * 25 + 24));
        assertEquals("ab.", last.substring(last.length() - 3));
    }

    @Test
    void numberInvertsPages() {
        BigInteger n = new BigInteger("98765432109876543210");
        assertEquals(n, BookText.number(BookText.pages(n, LAYOUT, ALPHABET), ALPHABET));
    }

    @Test
    void overflowWrapsModuloCapacity() {
        BigInteger capacity = LAYOUT.capacity(ALPHABET);
        assertEquals(BigInteger.valueOf(25).pow(60), capacity);
        assertEquals(BookText.pages(BigInteger.valueOf(7), LAYOUT, ALPHABET),
                BookText.pages(capacity.add(BigInteger.valueOf(7)), LAYOUT, ALPHABET));
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> BookText.pages(BigInteger.valueOf(-1), LAYOUT, ALPHABET));
        assertThrows(IllegalArgumentException.class, () -> new BookLayout(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new BookAlphabet("aa"));
    }

    private static String lastPage(BigInteger n) {
        List<String> pages = BookText.pages(n, LAYOUT, ALPHABET);
        return pages.get(pages.size() - 1);
    }
}
