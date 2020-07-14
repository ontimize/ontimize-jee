/**
 *
 */
package com.ontimize.jee.server.dao.jpa.dataconversors;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DataConversorsUtil.
 */
public final class DataConversorsUtil {

    private static List<DataConversor<Object, Object>> conversors = new ArrayList<>();

    public static List<DataConversor<Object, Object>> getConversors() {
        return DataConversorsUtil.conversors;
    }

    public static void setConversors(List<DataConversor<Object, Object>> conversors) {
        DataConversorsUtil.conversors = conversors;
    }

    /**
     * Convert.
     * @param input the input
     * @param toClass the to class
     * @return the object
     */
    public static Object convert(Object input, Class<?> toClass) {
        for (DataConversor<Object, Object> dc : DataConversorsUtil.conversors) {
            if (dc.canHandleConversion(input, (Class<Object>) toClass)) {
                return dc.convert(input, (Class<Object>) toClass);
            }
        }
        return input;
    }

}
