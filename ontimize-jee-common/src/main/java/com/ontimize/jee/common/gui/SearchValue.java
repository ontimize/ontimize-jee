package com.ontimize.jee.common.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Class used to specified some conditions in the sql statements filters<br>
 *
 */

public class SearchValue implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(SearchValue.class);

    public static final int LESS = 0;

    public static final int LESS_EQUAL = 1;

    public static final int EQUAL = 2;

    public static final int MORE_EQUAL = 3;

    public static final int MORE = 4;

    public static final int NULL = 5;

    public static final int NOT_EQUAL = 6;

    public static final int OR = 7;

    public static final int BETWEEN = 8;

    public static final int IN = 9;

    public static final int NOT_NULL = 10;

    public static final int EXISTS = 11;

    public static final int NOT_IN = 12;

    public static final int NOT_BETWEEN = 13;

    protected int condition = SearchValue.EQUAL;

    public static final String LESS_STR = "<";

    public static final String LESS_EQUAL_STR = "<=";

    public static final String EQUAL_STR = "=";

    public static final String MORE_EQUAL_STR = ">=";

    public static final String MORE_STR = ">";

    public static final String NULL_STR = "IS NULL";

    public static final String NULL_STR2 = "NULL";

    public static final String OR_STR = "OR";

    public static final String NOT_EQUAL_STR = "<>";

    public static final String EXISTS_STR = "EXISTS";

    public static final String BETWEEN_STR = "BETWEEN";

    public static final String NOT_BETWEEN_STR = "NOT BETWEEN";

    public static final String IN_STR = "IN";

    public static final String NOT_IN_STR = "NOT IN";

    public static final String NOT_NULL_STR = "IS NOT NULL";

    protected Object value = null;

    public SearchValue(int condition, Object value) {
        if ((condition != SearchValue.LESS) && (condition != SearchValue.EQUAL) && (condition != SearchValue.MORE)
                && (condition != SearchValue.LESS_EQUAL) && (condition != SearchValue.MORE_EQUAL)
                && (condition != SearchValue.NULL) && (condition != SearchValue.OR)
                && (condition != SearchValue.NOT_EQUAL) && (condition != SearchValue.BETWEEN)
                && (condition != SearchValue.IN) && (condition != SearchValue.NOT_IN)
                && (condition != SearchValue.EXISTS) && (condition != SearchValue.NOT_NULL)
                && (condition != SearchValue.NOT_BETWEEN)) {
            this.condition = SearchValue.EQUAL;
        } else {
            this.condition = condition;
        }
        this.value = value;
    }

    public int getCondition() {
        return this.condition;
    }

    public String getStringCondition() {
        return SearchValue.conditionIntToStr(this.condition);
    }

    public Object getValue() {
        return this.value;
    }

    public static int conditionStringToInt(String s) {
        if (s.equals(SearchValue.LESS_STR)) {
            return SearchValue.LESS;
        } else if (s.equals(SearchValue.MORE_STR)) {
            return SearchValue.MORE;
        } else if (s.equals(SearchValue.LESS_EQUAL_STR)) {
            return SearchValue.LESS_EQUAL;
        } else if (s.equals(SearchValue.MORE_EQUAL_STR)) {
            return SearchValue.MORE_EQUAL;
        } else if (s.equals(SearchValue.NULL_STR2) || s.equals(SearchValue.NULL_STR)) {
            return SearchValue.NULL;
        } else if (s.equals(SearchValue.OR_STR)) {
            return SearchValue.OR;
        } else if (s.equals(SearchValue.NOT_EQUAL_STR)) {
            return SearchValue.NOT_EQUAL;
        } else if (s.equals(SearchValue.BETWEEN_STR)) {
            return SearchValue.BETWEEN;
        } else if (s.equals(SearchValue.NOT_BETWEEN_STR)) {
            return SearchValue.NOT_BETWEEN;
        } else if (s.equals(SearchValue.IN_STR)) {
            return SearchValue.IN;
        } else if (s.equals(SearchValue.NOT_IN_STR)) {
            return SearchValue.NOT_IN;
        } else if (s.equals(SearchValue.NOT_NULL_STR)) {
            return SearchValue.NOT_NULL;
        } else if (s.equals(SearchValue.EXISTS_STR)) {
            return SearchValue.EXISTS;
        } else {
            return SearchValue.EQUAL;
        }
    }

    public static String conditionIntToStr(int i) {
        if (i == SearchValue.LESS) {
            return SearchValue.LESS_STR;
        } else if (i == SearchValue.MORE) {
            return SearchValue.MORE_STR;
        } else if (i == SearchValue.LESS_EQUAL) {
            return SearchValue.LESS_EQUAL_STR;
        } else if (i == SearchValue.MORE_EQUAL) {
            return SearchValue.MORE_EQUAL_STR;
        } else if (i == SearchValue.NULL) {
            return SearchValue.NULL_STR;
        } else if (i == SearchValue.OR) {
            return SearchValue.OR_STR;
        } else if (i == SearchValue.NOT_EQUAL) {
            return SearchValue.NOT_EQUAL_STR;
        } else if (i == SearchValue.BETWEEN) {
            return SearchValue.BETWEEN_STR;
        } else if (i == SearchValue.IN) {
            return SearchValue.IN_STR;
        } else if (i == SearchValue.NOT_IN) {
            return SearchValue.NOT_IN_STR;
        } else if (i == SearchValue.NOT_NULL) {
            return SearchValue.NOT_NULL_STR;
        } else if (i == SearchValue.EXISTS) {
            return SearchValue.EXISTS_STR;
        } else if (i == SearchValue.NOT_BETWEEN) {
            return SearchValue.NOT_BETWEEN_STR;
        } else {
            return SearchValue.EQUAL_STR;
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o == this) {
                return true;
            }
            if (!(o instanceof SearchValue)) {
                return false;
            }
            SearchValue current = (SearchValue) o;
            return current.value.equals(this.value) && (this.condition == current.condition);
        } catch (Exception ex) {
            SearchValue.logger.error(null, ex);
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
