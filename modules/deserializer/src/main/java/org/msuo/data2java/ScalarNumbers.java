package org.msuo.data2java;

import java.math.BigDecimal;

final class ScalarNumbers {

    private ScalarNumbers() {}

    static ScalarValue fromNumber(Number n) {
        if (n instanceof Integer || n instanceof Short || n instanceof Byte) {
            return ScalarValue.ofInt(n.intValue());
        }

        if (n instanceof Long) {
            long l = n.longValue();
            if (fitsInt(l)) return ScalarValue.ofInt((int) l);
            return ScalarValue.ofDouble((double) l);
        }

        if (n instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) n;
            try {
                return ScalarValue.ofInt(bd.intValueExact());
            } catch (ArithmeticException ignored) {
                return ScalarValue.ofDouble(bd.doubleValue());
            }
        }

        double d = n.doubleValue();
        if (fitsInt(d)) return ScalarValue.ofInt((int) d);
        return ScalarValue.ofDouble(d);
    }

    static boolean fitsInt(double d) {
        return d == Math.rint(d) && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE;
    }

    private static boolean fitsInt(long l) {
        return l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE;
    }
}
