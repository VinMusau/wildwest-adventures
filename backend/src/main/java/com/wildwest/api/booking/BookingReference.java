package com.wildwest.api.booking;

import java.security.SecureRandom;

final class BookingReference {

    private static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return "WWA-" + sb;
    }

    private BookingReference() {}
}