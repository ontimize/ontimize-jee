package com.ontimize.jee.server.dao.jpa.ql.jpql;

import com.ontimize.jee.server.dao.jpa.ql.AbstractQLConditionValuesProcessor;

public class DefaultJPQLConditionValuesProcessor extends AbstractQLConditionValuesProcessor {

    /**
     * Creates a <code>DefaultSQLConditionValuesProcessor</code> where every condition that uses
     * <code>LIKE</code> is case-insensitive.
     * <p>
     * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
     * UPPER(Value))
     * @param upperLike true if the LIKE condition should be case-insensitive
     */
    public DefaultJPQLConditionValuesProcessor(final boolean upperLike) {
        super(upperLike);
    }

    /**
     * Creates a <code>DefaultSQLConditionValuesProcessor</code> where every condition that uses
     * <code>LIKE</code> or is a column of String type is case-insensitive.
     * <p>
     * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
     * UPPER(Value)) Inserts a upper function in both sides if column type is a String (UPPER(Field)
     * LIKE UPPER(String))
     * @param upperStrings true if the String column type should be case-insensitive
     * @param upperLike true if the LIKE condition should be case-insensitive
     */
    public DefaultJPQLConditionValuesProcessor(final boolean upperStrings, final boolean upperLike) {
        super(upperStrings, upperLike);
    }

}
