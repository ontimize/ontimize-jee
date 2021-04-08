/*
 *
 */
package com.ontimize.jee.common.tools;

/**
 * Clase de utilidades matematicas.
 */
public final class MathTools {

    public final static float ONE_FLOAT = 1.0f;

    /**
     * Instantiates a new math utils.
     */
    private MathTools() {
        super();
    }

    /**
     * Obtiene el minimo comun multipo del array de enteros.
     * @param nums the nums
     * @return the int
     */
    public static int mcm(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        int tmp = nums[0];
        for (int i = 1; i < nums.length; i++) {
            tmp = MathTools.mcm(tmp, nums[i]);
        }
        return tmp;
    }

    /**
     * Obtiene el minimo comun multipo de dos numeros.
     * @param x the x
     * @param y the y
     * @return the int
     */
    public static int mcm(int x, int y) {
        return Math.abs((x * y) / MathTools.mcd(x, y));
    }

    /**
     * Obtieene el maximo comun divisor del array de enteros.
     * @param nums the nums
     * @return the int
     */
    public static int mcd(int[] nums) {
        if (nums.length == 1) {
            return nums[0];
        }
        int tmp = nums[0];
        for (int i = 1; i < nums.length; i++) {
            tmp = MathTools.mcd(tmp, nums[i]);
        }
        return tmp;
    }

    /**
     * Obtiene el maximo comun divisor de dos enteros.
     * @param x the x
     * @param y the y
     * @return the int
     */
    public static int mcd(int x, int y) {
        int r;
        r = x % y;
        while (r != 0) {
            x = y;
            y = r;
            r = x % y;
        }
        return Math.abs(y);
    }

}
