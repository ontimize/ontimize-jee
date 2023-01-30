package com.ontimize.jee.common.gui;

import java.security.SecureRandom;

/**
 * Creates random string with a specified length using letters and number
 */

public class RandomStringGenerator {

    private static final int MIN_LENGTH = 1;

    private static final int MAX_LENGTH = 100;

    private static final char[] CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'y', 'z'};

    private static final int CHARACTER_NUMBER = RandomStringGenerator.CHARACTERS.length;

    private RandomStringGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String generate(int length) {
        if ((length < RandomStringGenerator.MIN_LENGTH) || (length > RandomStringGenerator.MAX_LENGTH)) {
            throw new IllegalArgumentException("String length must be >= " + RandomStringGenerator.MIN_LENGTH
                    + " and <= " + RandomStringGenerator.MAX_LENGTH);
        }
        SecureRandom random = new SecureRandom();
        byte[] seed = random.generateSeed(10);
        random.setSeed(seed);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int characterIndex = random.nextInt(RandomStringGenerator.CHARACTER_NUMBER);
            char character = RandomStringGenerator.CHARACTERS[characterIndex];
            sb.append(character);
        }
        return sb.toString();
    }
}
