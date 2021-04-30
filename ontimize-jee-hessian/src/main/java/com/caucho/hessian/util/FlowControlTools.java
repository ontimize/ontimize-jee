package com.caucho.hessian.util;

public final class FlowControlTools {

    private FlowControlTools() {
        super();
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return (lower <= x) && (x <= upper);
    }

}
