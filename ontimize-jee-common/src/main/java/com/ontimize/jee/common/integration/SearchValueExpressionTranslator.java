/**
 * SearchValueExpressionTranslator.java 15-oct-2014
 *
 * Copyright 2014 Imatia.
 */
package com.ontimize.jee.common.integration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SearchValueExpressionTranslator.
 *
 * @author <a href="luis.garcia@imatia.com">Luis Garcia</a>
 */
public final class SearchValueExpressionTranslator {

    private static final Logger logger = LoggerFactory.getLogger(SearchValueExpressionTranslator.class);

    /**
     * Translate expression.
     * @param <T> the generic type
     * @param <S> the generic type
     * @param expression the expression
     * @param entityEnumTranslator the entity enum translator
     * @return the search value expression
     */
    public static <T extends EntityEnum, S extends EntityEnum> SearchValueExpression<T> translateExpression(
            final SearchValueExpression<S> expression,
            final EntityEnumTranslator<S, T> entityEnumTranslator) {
        if (expression == null) {
            return null;
        }
        final List<ISearchValue<T>> searchValues = new ArrayList<>();
        for (final ISearchValue<S> searchValue : expression.getSearchValues()) {
            if (searchValue instanceof SearchValue) {
                searchValues.add(SearchValueExpressionTranslator.translateSearchValue((SearchValue<S>) searchValue,
                        entityEnumTranslator));
            } else if (searchValue instanceof SearchValueExpression) {
                final SearchValueExpression<S> oldSearchValueExpression = (SearchValueExpression<S>) searchValue;
                searchValues.add(SearchValueExpressionTranslator.translateExpression(oldSearchValueExpression,
                        entityEnumTranslator));
            } else {
                SearchValueExpressionTranslator.logger
                    .error("SearchValueExpression contains an unsupported ISearchValue subclass. Only SearchValueExpression and SearchValue are supported!");
                throw new IllegalArgumentException(
                        "SearchValueExpression contains an unsupported ISearchValue subclass. Only SearchValueExpression and SearchValue are supported!");
            }
        }
        final SearchValueExpression<T> result = new SearchValueExpression<>(searchValues, expression.getOperator());
        return result;
    }

    /**
     * Translate search value.
     * @param <T> the generic type
     * @param <S> the generic type
     * @param searchValue the search value
     * @param entityEnumTranslator the entity enum translator
     * @return the search value
     */
    public static <T extends EntityEnum, S extends EntityEnum> SearchValue<T> translateSearchValue(
            final SearchValue<S> searchValue,
            final EntityEnumTranslator<S, T> entityEnumTranslator) {
        final SearchValue<T> newSearchValue = new SearchValue<>(entityEnumTranslator.translate(searchValue.getColumn()),
                searchValue.getQueryOperator(), searchValue.getValue());
        return newSearchValue;
    }

    /**
     * Instantiates a new search value expression translator.
     */
    private SearchValueExpressionTranslator() {
        super();
    }

}
