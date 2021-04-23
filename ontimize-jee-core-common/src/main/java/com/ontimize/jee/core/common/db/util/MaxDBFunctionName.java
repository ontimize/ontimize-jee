package com.ontimize.jee.core.common.db.util;

/**
 * Attribute to pass in query <code>attributes</code> when user wants to perform a
 * <code>SELECT MAX(columnName)FROM TABLE </code>
 *
 * @author Imatia Innovation SL
 * @since 5.2060EN-0.6
 */
public class MaxDBFunctionName extends DBFunctionName {

    public MaxDBFunctionName(String columnName) {
        super("MAX(" + columnName + ")");
    }

    public MaxDBFunctionName(String columnName, String alias, boolean isolation) {
        super("MAX(" + columnName + ") AS " + alias, isolation);
    }

}
