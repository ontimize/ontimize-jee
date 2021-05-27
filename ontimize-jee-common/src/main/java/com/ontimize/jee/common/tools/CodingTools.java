package com.ontimize.jee.common.tools;

/**
 * The Class CodingTools.
 */
public final class CodingTools {

    private static final int SIXTY_TWO = 62;

    private static final int FIFTY_TWO = 52;

    private static final int TWENTY_SIX = 26;

    private static final int SIX_BITS_MASK = 0x3f;

    private static final int EIGHTEEN = 18;

    private static final int TWELVE = 12;

    private static final int SIX = 6;

    private static final int ONE = 1;

    private static final int THREE = 3;

    private static final int TWO = 2;

    private static final int BYTE_SIZE = 8;

    private static final int TWO_BYTE_SIZE = 2 * CodingTools.BYTE_SIZE;

    /**
     * Instantiates a new coding tools.
     */
    private CodingTools() {
    }

    /**
     * Base64.
     * @param value the value
     * @return the string
     */
    public static String encodeBase64(String value) {
        StringBuilder cb = new StringBuilder();

        int i = 0;
        for (i = 0; (i + CodingTools.TWO) < value.length(); i += CodingTools.THREE) {
            long chunk = value.charAt(i);
            chunk = (chunk << CodingTools.BYTE_SIZE) + value.charAt(i + CodingTools.ONE);
            chunk = (chunk << CodingTools.BYTE_SIZE) + value.charAt(i + CodingTools.TWO);

            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.EIGHTEEN));
            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.TWELVE));
            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.SIX));
            cb.append(CodingTools.encodeBase64(chunk));
        }

        if ((i + 1) < value.length()) {
            long chunk = value.charAt(i);
            chunk = (chunk << CodingTools.BYTE_SIZE) + value.charAt(i + CodingTools.ONE);
            chunk <<= CodingTools.BYTE_SIZE;

            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.EIGHTEEN));
            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.TWELVE));
            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.SIX));
            cb.append('=');
        } else if (i < value.length()) {
            long chunk = value.charAt(i);
            chunk <<= CodingTools.TWO_BYTE_SIZE;

            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.EIGHTEEN));
            cb.append(CodingTools.encodeBase64(chunk >> CodingTools.TWELVE));
            cb.append('=');
            cb.append('=');
        }

        return cb.toString();
    }

    /**
     * Encode base64.
     * @param d the d
     * @return the char
     */
    public static char encodeBase64(long d) {
        d &= CodingTools.SIX_BITS_MASK;
        if (d < CodingTools.TWENTY_SIX) {
            return (char) (d + 'A');
        } else if (d < CodingTools.FIFTY_TWO) {
            return (char) ((d + 'a') - CodingTools.TWENTY_SIX);
        } else if (d < CodingTools.SIXTY_TWO) {
            return (char) ((d + '0') - CodingTools.FIFTY_TWO);
        } else if (d == CodingTools.SIXTY_TWO) {
            return '+';
        } else {
            return '/';
        }
    }

}
