/**
 * BigDecimalTools.java 21/08/2013
 *
 *
 *
 */
package com.ontimize.jee.common.tools;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The Class BigDecimalTools.
 *
 * @author <a href=""></a>
 */
public final class BigDecimalTools {

    /** The one. */
    public final static BigDecimal ONE = BigDecimal.ONE;

    /** The zero. */
    public final static BigDecimal ZERO = BigDecimal.ZERO;

    public final static BigDecimal TWO = new BigDecimal("2.0");

    /** The Constant E. */
    public final static BigDecimal E = new BigDecimal("2.71828182845904523536028747135266249775724709369995");

    /** The Constant PI. */
    public final static BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937510");

    /**
     * Instantiates a new big decimal tools.
     */
    private BigDecimalTools() {
        super();
    }

    /**
     * Sin.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal sin(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.sin(d), mc);
    }

    /**
     * Cos.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal cos(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.cos(d), mc);
    }

    /**
     * Tan.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal tan(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.tan(d));
    }

    /**
     * Abs.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal abs(BigDecimal bd) {
        if (bd.signum() == -1) {
            bd = bd.negate();
        }
        return bd;
    }

    /**
     * Asin.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal asin(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.asin(d), mc);
    }

    /**
     * Acos.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal acos(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.acos(d), mc);
    }

    /**
     * Atan.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal atan(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.atan(d), mc);
    }

    /**
     * Atan2.
     * @param bd1 the bd1
     * @param bd2 the bd2
     * @return the big decimal
     */
    public static BigDecimal atan2(BigDecimal bd1, BigDecimal bd2, MathContext mc) {
        double d1 = bd1.doubleValue();
        double d2 = bd2.doubleValue();
        return new BigDecimal(Math.atan2(d1, d2), mc);
    }

    /**
     * Floor.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal floor(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.floor(d), mc);
    }

    /**
     * Ceil.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal ceil(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.ceil(d), mc);
    }

    /**
     * Exp.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal exp(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.exp(d), mc);
    }

    /**
     * NATURAL log. (Log base e)
     * @param bd the bd
     * @return Natural log of bd
     */
    public static BigDecimal log(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        double result = 0.0;
        result = Math.log(d);
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return new BigDecimal("0");
        }
        return new BigDecimal(Math.log(d));
    }

    /**
     * COMMON log. (Log base 10)
     * @param x the x
     * @return Common log of bd
     */
    public static BigDecimal log10(BigDecimal x, MathContext mc) {
        BigDecimal ret = BigDecimalTools.log(x, mc);
        BigDecimal denom = BigDecimalTools.log(BigDecimal.TEN, mc);
        BigDecimal result = ret.divide(denom, mc);
        return result;
    }

    // hyperbolic sin
    /**
     * Sinh.
     * @param x the x
     * @return the big decimal
     */
    public static BigDecimal sinh(BigDecimal x, MathContext mc) {
        return BigDecimalTools.exp(x, mc).subtract(x.negate()).divide(BigDecimalTools.TWO, mc);
    }

    // hyperbolic cos
    /**
     * Cosh.
     * @param x the x
     * @return the big decimal
     */
    public static BigDecimal cosh(BigDecimal x, MathContext mc) {
        return BigDecimalTools.exp(x, mc).add(x.negate()).divide(BigDecimalTools.TWO, mc);
    }

    // hyperbolic tan
    /**
     * Tanh.
     * @param x the x
     * @return the big decimal
     */
    public static BigDecimal tanh(BigDecimal x, MathContext mc) {
        return BigDecimalTools.sinh(x, mc).divide(BigDecimalTools.cosh(x, mc), mc);
    }

    /**
     * Sqrt.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal sqrt(BigDecimal bd, MathContext mc) {
        double d = bd.doubleValue();
        return new BigDecimal(Math.sqrt(d), mc);
    }

    /**
     * Pow.
     * @param bd1 the bd1
     * @param bd2 the bd2
     * @return the big decimal
     */
    public static BigDecimal pow(BigDecimal bd1, BigDecimal bd2, MathContext mc) {
        double d1 = bd1.doubleValue();
        double d2 = bd2.doubleValue();
        double result = Math.pow(d1, d2);
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            throw new ArithmeticException();
        }
        return new BigDecimal(result, mc);
    }

    /**
     * Fac.
     * @param bd the bd
     * @return the big decimal
     */
    public static BigDecimal fac(BigDecimal bd, MathContext mc) {
        // TODO: if bd is not a positive integer, throw an error or something, for now, will just
        // convert (by flooring) and go from there
        bd = BigDecimalTools.floor(bd, mc);
        return BigDecimalTools.facRecursive(bd, mc);
    }

    /**
     * Fac recursive.
     * @param bd the bd
     * @return the big decimal
     */
    private static BigDecimal facRecursive(BigDecimal bd, MathContext mc) {
        if ((bd.compareTo(BigDecimalTools.ONE) == 0) || (bd.compareTo(BigDecimalTools.ZERO) == 0)) {
            return BigDecimalTools.ONE;
        } else {
            return bd.multiply(BigDecimalTools.facRecursive(bd.subtract(BigDecimalTools.ONE), mc));
        }
    }

    /**
     * Fac.
     * @param d the d
     * @return the double
     */
    public static double fac(double d) {
        // TODO: if bd is not a positive integer, throw an error or something, for now, will just
        // convert (by flooring) and go from there
        d = Math.floor(d);
        return BigDecimalTools.facRecursive(d);
    }

    /**
     * Fac recursive.
     * @param d the d
     * @return the double
     */
    private static double facRecursive(double d) {
        if ((Double.compare(d, 1.0d) == 0) || (Double.compare(d, 0.0d) == 0)) {
            return 1.0;
        } else {
            return d * BigDecimalTools.facRecursive(d - 1.0);
        }
    }

}
