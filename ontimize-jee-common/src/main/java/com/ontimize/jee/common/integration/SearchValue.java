/**
 *
 */
package com.ontimize.jee.common.integration;

/**
 * The Class SearchValue.
 *
 * @param <T> the generic type
 */
public class SearchValue<T extends EntityEnum> implements ISearchValue<T> {

    /** The column. */
    private final T column;

    /** The query operator. */
    private final QueryOperator queryOperator;

    /** The value. */
    private final Object value;

    /**
     * Instantiates a new search value.
     * @param column the column
     * @param queryOperator the query operator
     * @param value the value
     */
    public SearchValue(T column, QueryOperator queryOperator, Object value) {
        super();
        this.column = column;
        this.queryOperator = queryOperator;
        this.value = value;
    }

    /**
     * Instantiates a new search value.
     * @param column the column
     * @param queryOperator the query operator
     */
    public SearchValue(T column, QueryOperator queryOperator) {
        super();
        this.column = column;
        this.queryOperator = queryOperator;
        this.value = null;
    }

    /**
     * Gets the column.
     * @return the column
     */
    public T getColumn() {
        return this.column;
    }

    /**
     * Gets the query operator.
     * @return the query operator
     */
    public QueryOperator getQueryOperator() {
        return this.queryOperator;
    }

    /**
     * Gets the value.
     * @return the value
     */
    public Object getValue() {
        return this.value;
    }

}
