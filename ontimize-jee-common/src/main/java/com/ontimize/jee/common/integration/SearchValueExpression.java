/**
 *
 */
package com.ontimize.jee.common.integration;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class SearchValueExpression.
 * 
 * @param <T>
 *            the generic type
 */
public class SearchValueExpression<T extends EntityEnum> implements ISearchValue<T> {

    /** The search values. */
    private List<ISearchValue<T>> searchValues = new ArrayList<ISearchValue<T>>();

    /** The operator. */
    private final Operator operator;

    /**
     * Instantiates a new search value expression.
     * 
     * @param searchValues
     *            the search values
     * @param operator
     *            the operator
     */
    public SearchValueExpression(List<ISearchValue<T>> searchValues, Operator operator) {
        super();
        if (searchValues != null) {
            this.searchValues.addAll(searchValues);
        }
        this.operator = operator;
    }

    /**
     * Instantiates a new search value expression.
     * 
     * @param searchValues
     *            the search values
     */
    public SearchValueExpression(List<ISearchValue<T>> searchValues) {
        super();
        this.searchValues = searchValues;
        this.operator = Operator.AND;
    }

    /**
     * Gets the search values.
     * 
     * @return the search values
     */
    public List<ISearchValue<T>> getSearchValues() {
        return this.searchValues;
    }

    /**
     * Gets the operator.
     * 
     * @return the operator
     */
    public Operator getOperator() {
        return this.operator;
    }

}
