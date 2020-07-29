/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql;

import java.util.List;
import java.util.Map;

/**
 * The Interface QLConditionValuesProcessor.
 */
public interface QLConditionValuesProcessor {

    /**
     * Sets the value to ql literal processor.
     * @param valueToQLProcessor the new value to ql processor
     */
    void setValueToQLLiteralProcessor(ValueToQLLiteralProcessor valueToQLProcessor);

    /**
     * Gets the value to ql literal processor.
     * @return the value to ql literal processor
     */
    ValueToQLLiteralProcessor getValueToQLLiteralProcessor();

    /**
     * Creates the query conditions.
     * @param conditions the conditions
     * @param wildcards the wildcards which conditions do are allowed to have wildcards
     * @return the string
     */
    String createQueryConditions(Map<?, ?> conditions, List<String> wildcards);

}
