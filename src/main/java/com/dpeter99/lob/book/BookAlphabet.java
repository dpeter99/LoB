package com.dpeter99.lob.book;

/**
 * The symbol set a book's text is written in. Borges' library uses 25 symbols: 22 letters,
 * the space, the comma and the period. Digit 0 is the space so padding reads as blank.
 * <p>
 * Pure: no registry or level types, so JUnit can use it directly.
 */
public final class BookAlphabet {
    /** 22 letters (no j, k, w, x), then comma and period; the space is prepended as digit 0. */
    public static final BookAlphabet BORGES = new BookAlphabet(" abcdefghilmnopqrstuvyz,.");

    private final String symbols;

    public BookAlphabet(String symbols) {
        if (symbols.length() < 2) {
            throw new IllegalArgumentException("an alphabet needs at least two symbols");
        }
        if (symbols.chars().distinct().count() != symbols.length()) {
            throw new IllegalArgumentException("alphabet symbols must be distinct: " + symbols);
        }
        this.symbols = symbols;
    }

    public int radix() {
        return symbols.length();
    }

    public char symbol(int digit) {
        return symbols.charAt(digit);
    }

    /** Inverse of {@link #symbol}; -1 when the character is not in the alphabet. */
    public int digit(char symbol) {
        return symbols.indexOf(symbol);
    }
}
