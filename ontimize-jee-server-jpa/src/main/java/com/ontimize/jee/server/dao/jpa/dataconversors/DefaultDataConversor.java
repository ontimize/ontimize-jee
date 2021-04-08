/**
 *
 */
package com.ontimize.jee.server.dao.jpa.dataconversors;

import java.math.BigDecimal;

import com.ontimize.db.NullValue;
import com.ontimize.jee.common.tools.ParseUtilsExtended;

/**
 * The Class DefaultDataConversor.
 */
public class DefaultDataConversor implements DataConversor<Object, Object> {

    /**
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.dao.jpa.dataconversors.DataConversor#convert(java.lang.Object,
     *      java.lang.Class)
     */
    @Override
    public Object convert(Object input, Class<Object> toType) {
        if (input instanceof NullValue) {
            return null;
        }
        if (toType.equals(Boolean.class)) {
            return this.convertToBoolean(input);
        } else if (toType.equals(Integer.class)) {
            return this.convertToInteter(input);
        } else if (toType.equals(Long.class)) {
            return this.convertToLong(input);
        } else if (toType.equals(Double.class)) {
            return this.convertToDouble(input);
        } else if (toType.equals(BigDecimal.class)) {
            return this.convertToBigDecimal(input);
        }
        return input;
    }

    private Object convertToBoolean(Object input) {
        if ((input instanceof Boolean) || (input == null)) {
            return input;
        }
        return ParseUtilsExtended.getBoolean(input);
    }

    private Object convertToInteter(Object input) {
        if ((input instanceof Integer) || (input == null)) {
            return input;
        }
        return Integer.valueOf(input.toString());
    }

    private Object convertToLong(Object input) {
        if ((input instanceof Long) || (input == null)) {
            return input;
        }
        return Long.valueOf(input.toString());
    }

    private Object convertToDouble(Object input) {
        if ((input instanceof Double) || (input == null)) {
            return input;
        }
        return Double.valueOf(input.toString());
    }

    private Object convertToBigDecimal(Object input) {
        if ((input instanceof BigDecimal) || (input == null)) {
            return input;
        } else if (input instanceof Integer) {
            return BigDecimal.valueOf((Integer) input);
        } else if (input instanceof Long) {
            return BigDecimal.valueOf((Long) input);
        } else if (input instanceof Double) {
            return BigDecimal.valueOf((Double) input);
        }
        return BigDecimal.ZERO;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.dao.jpa.dataconversors.DataConversor#canHandleConversion(java.lang.Object,
     *      java.lang.Class)
     */
    @Override
    public boolean canHandleConversion(Object object, Class<Object> toType) {
        return true;
    }

}
