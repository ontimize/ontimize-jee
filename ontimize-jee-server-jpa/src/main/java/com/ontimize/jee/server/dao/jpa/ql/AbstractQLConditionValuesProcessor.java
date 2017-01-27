/**
 *
 */
package com.ontimize.jee.server.dao.jpa.ql;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.NullValue;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.NoWildCard;
import com.ontimize.db.SQLStatementBuilder.SQLExpression;
import com.ontimize.db.SQLStatementBuilder.SQLNameEval;
import com.ontimize.gui.SearchValue;

public abstract class AbstractQLConditionValuesProcessor implements QLConditionValuesProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractQLConditionValuesProcessor.class);

    private QLNameEval qlNameEval = null;

    private boolean upperLike = false;
    private boolean upperStrings = false;

    protected char[] CONFLICT_CHARS = { ' ', '/', '-', 'Á', 'É', 'Í', 'Ó', 'Ú', 'á', 'é', 'í', 'ó', 'ú', '%' };

    private String upperFunction = "UPPER";

    private ValueToQLLiteralProcessor valueToQLProcessor;
    
    protected String beanPrefix = null;

	/**
     * Creates a <code>AbstractQLConditionValuesProcessor</code> where every
     * condition that uses <code>LIKE</code> is case-insensitive.
     * <p>
     * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE UPPER(Value))
     *
     * @param upperLike
     *            true if the LIKE condition should be case-insensitive
     */
    public AbstractQLConditionValuesProcessor(final boolean upperLike) {
        this.upperLike = upperLike;
    }

    /**
     * Creates a <code>AbstractQLConditionValuesProcessor</code> where every
     * condition that uses <code>LIKE</code> or is a column of String type
     * is case-insensitive.
     * <p>
     * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE UPPER(Value)) Inserts a upper function in both sides if column type is a String (UPPER(Field) LIKE UPPER(String))
     *
     * @param upperStrings
     *            true if the String column type should be case-insensitive
     * @param upperLike
     *            true if the LIKE condition should be case-insensitive
     */
    public AbstractQLConditionValuesProcessor(final boolean upperStrings, final boolean upperLike) {
        this.upperLike = upperLike;
        this.upperStrings = upperStrings;
    }

    @Override
    public void setValueToQLLiteralProcessor(ValueToQLLiteralProcessor valueToQLProcessor) {
        this.valueToQLProcessor = valueToQLProcessor;
    }

    @Override
    public ValueToQLLiteralProcessor getValueToQLLiteralProcessor() {
        return this.valueToQLProcessor;
    }

    @Override
    public String createQueryConditions(final Map<?, ?> conditions, final List<String> wildcards) {
        StringBuilder sbStringQuery = new StringBuilder();
        final Iterator<?> enumKeys = conditions.keySet().iterator();
        while (enumKeys.hasNext()) {
            final Object oKey = enumKeys.next();
            final Object oValue = conditions.get(oKey);
            if (oKey instanceof String) {
                final boolean isLast = !enumKeys.hasNext();
                final boolean encloseColumnName = this.checkColumnName((String) oKey);
                // Check wildcards:
                if (oValue instanceof String) {
                    sbStringQuery = this.appendStringComparisonClause(sbStringQuery, (String) oKey, (String) oValue, isLast, encloseColumnName, ((wildcards == null) || wildcards.isEmpty() || wildcards.contains(oKey)));
                } else if (oValue instanceof SearchValue) {
                    sbStringQuery = this.addSearchValue(sbStringQuery, (String) oKey, (SearchValue) oValue, isLast, encloseColumnName);
                } else if (oValue instanceof NoWildCard) {
                    sbStringQuery = this.appendStringComparisonClause(sbStringQuery, (String) oKey, ((NoWildCard) oValue).getValue(), isLast, encloseColumnName, false);
                } else {
                    String lastOperator = null;
                    if (!isLast) {
                        lastOperator = SQLStatementBuilder.AND;
                    }
                    this.addColumnValue(sbStringQuery, (String) oKey, oValue, lastOperator, encloseColumnName);
                }

            }
        }
        return sbStringQuery.toString();
    }

    protected StringBuilder appendStringComparisonClause(final StringBuilder sbStringQuery, final String oKey, final String oValue, final boolean isLast, final boolean encloseColumnName, final boolean wildcarded) {
        String targetValue = "'" + oValue + "'";
        StringBuilder sbStringQueryInner = sbStringQuery;
        
        if (valueToQLProcessor!=null){
        	targetValue = valueToQLProcessor.toQLLiteral(oValue);
        }

        String likeOperator = SQLStatementBuilder.LIKE;
        if (targetValue.indexOf(SQLStatementBuilder.NOT_EQUAL_ID) == 0) {
            likeOperator = SQLStatementBuilder.NOT_LIKE;
            targetValue = targetValue.substring(SQLStatementBuilder.NOT_EQUAL_ID.length());
        }

        if (wildcarded && ((targetValue.indexOf(SQLStatementBuilder.ASTERISK_CHAR) >= 0) || (targetValue.indexOf(SQLStatementBuilder.INTERROG) >= 0))) {
            // Add sort values. We use '%' and '_' instead of
            // '*' and
            // '?'
            targetValue = targetValue.replace(SQLStatementBuilder.ASTERISK_CHAR, SQLStatementBuilder.PERCENT);
            targetValue = targetValue.replace(SQLStatementBuilder.INTERROG, SQLStatementBuilder.LOW_LINE);
        } else {
            // There are no Wildcards
        }

        if (this.upperLike) {
            sbStringQueryInner.append(this.upperFunction);
            sbStringQueryInner.append(SQLStatementBuilder.OPEN_PARENTHESIS);
            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
            sbStringQueryInner.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        } else {
            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
        }
        if (wildcarded) {
			sbStringQueryInner.append(likeOperator);
		} else {
			sbStringQueryInner.append(" = ");
		}
        if (this.upperLike) {
            sbStringQueryInner.append(this.upperFunction);
            sbStringQueryInner.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        }
        
//        sbStringQueryInner.append("'");
        sbStringQueryInner.append(targetValue);
//        sbStringQueryInner.append("'");
        if (this.upperLike) {
            sbStringQueryInner.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        }

        if (!isLast) {
            sbStringQueryInner.append(SQLStatementBuilder.AND);
        }

        return sbStringQueryInner;
    }

    protected StringBuilder addSearchValue(final StringBuilder sbStringQuery, final String oKey, final SearchValue oValue, final boolean isLast, final boolean encloseColumnName) {
        StringBuilder sbStringQueryInner = sbStringQuery;
        if (oValue == null) {
            return sbStringQueryInner;
        } else if (oValue.getCondition() == SearchValue.OR) {
            sbStringQueryInner = this.appendOrClause(oKey, oValue, encloseColumnName, sbStringQueryInner);
        } else if ((oValue.getCondition() == SearchValue.IN) || (oValue.getCondition() == SearchValue.NOT_IN)) {
            sbStringQueryInner = this.appendInClause(oKey, oValue, encloseColumnName, sbStringQueryInner);
        } else if (oValue.getCondition() == SearchValue.EXISTS) {
            sbStringQueryInner = this.appendExistsClause(oKey, oValue, encloseColumnName, sbStringQueryInner);
        } else if ((oValue.getCondition() == SearchValue.BETWEEN) || (oValue.getCondition() == SearchValue.NOT_BETWEEN)) {
            sbStringQueryInner = this.appendBetweenClause(oKey, oValue, encloseColumnName, sbStringQueryInner);
        } else {
            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
            sbStringQueryInner.append(' ');
            sbStringQueryInner.append(SearchValue.conditionIntToStr(oValue.getCondition()));
            sbStringQueryInner.append(' ');
            if (oValue.getValue() != null) {
                sbStringQueryInner = this.addValue(sbStringQueryInner, oValue.getValue());
            }
            // else we don't add value (perhaps sql injection, be careful)
        }

        if (!isLast) {
            sbStringQueryInner.append(SQLStatementBuilder.AND);
        }
        return sbStringQueryInner;
    }

    /**
     * Append or clause.
     *
     * @param oKey
     *            the o key
     * @param oValue
     *            the o value
     * @param encloseColumnName
     *            the enclose column name
     * @param sbStringQueryInner
     *            the sb string query inner
     * @return the string builder
     */
    protected StringBuilder appendOrClause(final String oKey, final SearchValue oValue, final boolean encloseColumnName, StringBuilder sbStringQueryInner) {
        // If it is OR, then value is a vector with
        // objects
        final Object oSearchValue = oValue.getValue();
        if ((oSearchValue instanceof List) && !((List<?>) oSearchValue).isEmpty()) {
            final List<?> vSearchValues = (List<?>) oSearchValue;
            // Here the query;
            sbStringQueryInner.append(SQLStatementBuilder.OPEN_PARENTHESIS);
            for (int vs = 0; vs < vSearchValues.size(); vs++) {
                String lastOperatorToAppend = null;
                if (vs < (vSearchValues.size() - 1)) {
                    lastOperatorToAppend = SQLStatementBuilder.OR;
                }
                sbStringQueryInner = this.addColumnValue(sbStringQueryInner, oKey, vSearchValues.get(vs), lastOperatorToAppend, encloseColumnName);

            }
            sbStringQueryInner.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        } else {
            return sbStringQueryInner;
        }
        return sbStringQueryInner;
    }

    /**
     * Append in clause.
     *
     * @param oKey
     *            the o key
     * @param oValue
     *            the o value
     * @param encloseColumnName
     *            the enclose column name
     * @param sbStringQueryInner
     *            the sb string query inner
     * @return the string builder
     */
    protected StringBuilder appendInClause(final String oKey, final SearchValue oValue, final boolean encloseColumnName, final StringBuilder sbStringQueryInner) {
        return this.appendInAndExistsClause(oKey, oValue.getValue(), SearchValue.conditionIntToStr(oValue.getCondition()), encloseColumnName, sbStringQueryInner);
    }

    /**
     * Append exists clause.
     *
     * @param oKey
     *            the o key
     * @param oValue
     *            the o value
     * @param encloseColumnName
     *            the enclose column name
     * @param sbStringQueryInner
     *            the sb string query inner
     * @return the string builder
     */
    protected StringBuilder appendExistsClause(final String oKey, final SearchValue oValue, final boolean encloseColumnName, final StringBuilder sbStringQueryInner) {
        return this.appendInAndExistsClause(oKey, oValue.getValue(), SearchValue.conditionIntToStr(oValue.getCondition()), encloseColumnName, sbStringQueryInner);
    }

    private StringBuilder appendInAndExistsClause(final String oKey, final Object oSearchValue, String condition, final boolean encloseColumnName, StringBuilder sbStringQueryInner) {
        // If it is IN, then value is a vector with
        // Objects
        if (oSearchValue instanceof List) {
            if (!((List<?>) oSearchValue).isEmpty()) {
                final List<?> vSearchValue = (List<?>) oSearchValue;

                sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
                sbStringQueryInner.append(' ');
                sbStringQueryInner.append(condition);
                sbStringQueryInner.append(' ');
                StringBuilder sb = new StringBuilder();
                sb = this.addValue(sb, vSearchValue);
                sbStringQueryInner = this.addValueBetweenParenthesis(sbStringQueryInner, sb);
            } else {
                sbStringQueryInner.append('1').append(SQLStatementBuilder.NOT_EQUAL).append('1');
            }
        } else if (oSearchValue instanceof String) {
            final String sQuery = (String) oSearchValue;
            boolean putParenthesis = true;
            if (SQLStatementBuilder.checkParenthesis && this.hasParenthesis(sQuery)) {
                putParenthesis = false;
            }
            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
            sbStringQueryInner.append(' ');
            sbStringQueryInner.append(condition);
            sbStringQueryInner.append(' ');
            if (putParenthesis) {
                StringBuilder sb = new StringBuilder();
                sb = this.addValue(sb, sQuery);
                sbStringQueryInner = this.addValueBetweenParenthesis(sbStringQueryInner, sb);
            } else {
                sbStringQueryInner = this.addValue(sbStringQueryInner, sQuery);
            }
        } else if (oSearchValue instanceof SQLExpression) {
            // we are asuming that sqlexpresion is JPQL compliant
            final SQLExpression exp = ((SQLExpression) oSearchValue);
            boolean putParenthesis = true;
            if (SQLStatementBuilder.checkParenthesis && this.hasParenthesis(exp.getQuery())) {
                putParenthesis = false;
            }

            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
            sbStringQueryInner.append(' ');
            sbStringQueryInner.append(condition);
            sbStringQueryInner.append(' ');
            if (putParenthesis) {
                StringBuilder sb = new StringBuilder();
                sb = this.addValue(sb, exp);
                sbStringQueryInner = this.addValueBetweenParenthesis(sbStringQueryInner, sb);
            } else {
                sbStringQueryInner = this.addValue(sbStringQueryInner, exp);
            }

        }
        return sbStringQueryInner;
    }

    /**
     * Append between clause.
     *
     * @param oKey
     *            the o key
     * @param oValue
     *            the o value
     * @param encloseColumnName
     *            the enclose column name
     * @param sbStringQueryInner
     *            the sb string query inner
     * @return the string builder
     */
    protected StringBuilder appendBetweenClause(final String oKey, final SearchValue oValue, final boolean encloseColumnName, StringBuilder sbStringQueryInner) {
        final Object oSearchValue = oValue.getValue();
        if ((oSearchValue instanceof List) && (((List<?>) oSearchValue).size() == 2) && (((List<?>) oSearchValue).get(0) != null) && (((List<?>) oSearchValue).get(1) != null)) {
            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
            sbStringQueryInner.append(' ');
            sbStringQueryInner.append(SearchValue.conditionIntToStr(oValue.getCondition()));
            sbStringQueryInner.append(' ');
            sbStringQueryInner = this.addValue(sbStringQueryInner, ((List<?>) oSearchValue).get(0));
            sbStringQueryInner.append(SQLStatementBuilder.AND);
            sbStringQueryInner = this.addValue(sbStringQueryInner, ((List<?>) oSearchValue).get(1));
        } else {
            return sbStringQueryInner;
        }
        return sbStringQueryInner;
    }

    protected StringBuilder addValueBetweenParenthesis(final StringBuilder sbStringQuery, final StringBuilder objectValue) {
        StringBuilder sbStringQueryInner = sbStringQuery;
        sbStringQueryInner.append(SQLStatementBuilder.OPEN_PARENTHESIS);
        sbStringQueryInner.append(objectValue);
        sbStringQueryInner.append(SQLStatementBuilder.CLOSE_PARENTHESIS);
        return sbStringQueryInner;
    }

    protected StringBuilder addColumnValue(final StringBuilder sbStringQuery, final String oKey, final Object objectValue, final String lastOperatorToAppend, final boolean encloseColumnName) {
        StringBuilder sbStringQueryInner = sbStringQuery;
        if ((objectValue != null) && !(objectValue instanceof NullValue)) {
            if (objectValue instanceof List) {
                this.appendInAndExistsClause(oKey, objectValue, SearchValue.conditionIntToStr(SearchValue.IN), encloseColumnName, sbStringQueryInner);
            } else {
                sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
                sbStringQueryInner.append(SQLStatementBuilder.EQUAL);
                sbStringQueryInner = this.addValue(sbStringQueryInner, objectValue);
            }
        } else {
            sbStringQueryInner = this.encloseColumnName(sbStringQueryInner, oKey, encloseColumnName);
            sbStringQueryInner.append(SQLStatementBuilder.IS_NULL);
        }
        if (lastOperatorToAppend != null) {
            sbStringQueryInner.append(lastOperatorToAppend);
        }
        return sbStringQueryInner;
    }

    protected StringBuilder addValue(StringBuilder sbStringQueryInner, Object value) {
        if (this.valueToQLProcessor != null) {
            sbStringQueryInner.append(this.valueToQLProcessor.toQLLiteral(value));
        } else {
            if ((value == null) || (value instanceof NullValue)) {
                sbStringQueryInner.append(SQLStatementBuilder.NULL);
            } else if (value instanceof String) {
                sbStringQueryInner.append("'" + value.toString() + "'");
            } else {
                sbStringQueryInner.append(value.toString());
            }
        }
        return sbStringQueryInner;
    }

    protected boolean hasParenthesis(String sqlquery) {
        if ((sqlquery == null) || (sqlquery.length() == 0)) {
            return false;
        }
        String query = sqlquery.trim();

        return (('(' == query.charAt(0)) && (')' == query.charAt(query.length() - 1)));
    }

    /**
     * Check if column name must be enclosed. Returns true if square brackets are necessary insert in this column name.
     * <p>
     * If the column name has a character of the special character list, it's necessary insert column name in square brackets.
     *
     * @return true if square brackets are necessary insert in this column name.
     */
    protected boolean checkColumnName(final String columnName) {
        if (this.qlNameEval != null) {
            return this.qlNameEval.needCorch(columnName);
        }
        boolean bBrackets = false;
        if (columnName.toUpperCase().indexOf(" AS ") >= 0) {
            // since 5.2071EN-0.2
            final String columnNameNoAs = columnName.toUpperCase().replaceAll(" AS ", "");
            if (columnNameNoAs.indexOf(' ') >= 0) {
                return true;
            }
            //
            return false;
        }
        if ((columnName.indexOf(',') >= 0) || (columnName.indexOf('[') >= 0)) {
            return false;
        }
        for (int i = 0; i < this.CONFLICT_CHARS.length; i++) {
            if (columnName.indexOf(this.CONFLICT_CHARS[i]) >= 0) {
                bBrackets = true;
                break;
            }
        }
        return bBrackets;
    }

    /**
     * Adds characters to the list of special characters.
     * <p>
     * When the query is be created if a column name contains a character of the list this column name is inserted between parenthesis in the query.
     *
     * @param c
     *            a array with the new characters
     */
    public void addSpecialCharacters(final char[] c) {
        final char[] cNew = new char[this.CONFLICT_CHARS.length + c.length];
        System.arraycopy(this.CONFLICT_CHARS, 0, cNew, 0, this.CONFLICT_CHARS.length);
        System.arraycopy(c, 0, cNew, this.CONFLICT_CHARS.length, c.length);
        this.CONFLICT_CHARS = cNew;
        AbstractQLConditionValuesProcessor.logger.info("Special characters have been established: {}", cNew);
    }

    /**
     * Enclose column name.
     *
     * @param sbStringQuery
     *            the sb string query
     * @param oKey
     *            the o key
     * @param encloseColumnName
     *            the enclose column name
     * @return the string builder
     */
    protected StringBuilder encloseColumnName(final StringBuilder sbStringQuery, final String oKey, final boolean encloseColumnName) {
        if (encloseColumnName) {
            sbStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
            sbStringQuery.append(oKey);
            sbStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
        } else {
            sbStringQuery.append(oKey);
        }
        return sbStringQuery;
    }

    protected final void setQlNameEval(QLNameEval qlNameEval) {
        this.qlNameEval = qlNameEval;
    }

    public static interface QLNameEval extends SQLNameEval {
        @Override
        public boolean needCorch(String s);
    }

    public boolean isUpperLike() {
        return this.upperLike;
    }

    public void setUpperLike(boolean upperLike) {
        this.upperLike = upperLike;
    }

    public boolean isUpperStrings() {
        return this.upperStrings;
    }

    public void setUpperStrings(boolean upperStrings) {
        this.upperStrings = upperStrings;
    }

    public String getUpperFunction() {
        return this.upperFunction;
    }

    public void setUpperFunction(String upperFunction) {
        this.upperFunction = upperFunction;
    }

    public void setValueToQLProcessor(ValueToQLLiteralProcessor valueToQLProcessor) {
        this.valueToQLProcessor = valueToQLProcessor;
    }

    public String getBeanPrefix() {
		return beanPrefix;
	}

	public void setBeanPrefix(String beanPrefix) {
		this.beanPrefix = beanPrefix;
	}
}
