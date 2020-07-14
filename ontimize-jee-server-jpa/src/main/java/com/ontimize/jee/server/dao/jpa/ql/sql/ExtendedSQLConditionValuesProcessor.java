package com.ontimize.jee.server.dao.jpa.ql.sql;

import java.util.List;
import java.util.Map;

import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.db.SQLStatementBuilder.Expression;
import com.ontimize.db.SQLStatementBuilder.Field;

public class ExtendedSQLConditionValuesProcessor extends DefaultSQLConditionValuesProcessor {

    /**
     * Identifier used as condition key when has a complex expression.
     */
    public static final String EXPRESSION_KEY = "EXPRESSION_KEY_UNIQUE_IDENTIFIER";

    public ExtendedSQLConditionValuesProcessor() {
        super(false);
    }

    /**
     * Creates a <code>ExtendedSQLConditionValuesProcessor</code> where every condition that uses
     * <code>LIKE</code> is case-insensitive.
     * <p>
     * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
     * UPPER(Value))
     * @param upperLike true if the LIKE condition should be case-insensitive
     */

    public ExtendedSQLConditionValuesProcessor(final boolean upperLike) {
        super(upperLike);
    }

    /**
     * Creates a <code>ExtendedSQLConditionValuesProcessor</code> where every condition that uses
     * <code>LIKE</code> or is a column of String type is case-insensitive.
     * <p>
     * Inserts a upper function in both sides of <code>LIKE</code> conditions (UPPER(Field) LIKE
     * UPPER(Value)) Inserts a upper function in both sides if column type is a String (UPPER(Field)
     * LIKE UPPER(String))
     * @param upperStrings true if the String column type should be case-insensitive
     * @param upperLike true if the LIKE condition should be case-insensitive
     */

    public ExtendedSQLConditionValuesProcessor(final boolean upperStrings, final boolean upperLike) {
        super(upperStrings, upperLike);
    }

    /**
     * Returns the column name in square bracket if the name contains a special characters
     * @param name column name to checks
     * @return the column name in square bracket if it's necessary
     */
    protected String getColumnName(final String name) {
        final boolean corchetes = this.checkColumnName(name);
        if (!corchetes) {
            return name;
        } else {
            return SQLStatementBuilder.OPEN_SQUARE_BRACKET + name + SQLStatementBuilder.CLOSE_SQUARE_BRACKET;
        }
    }

    /**
     * Creates the condition string from a expression and stores required values in a vector.
     * @param expression a class that implements <code>Expression<code> interface
     * &#64;param values
     *            a list where the required values for the condition are stored
     * &#64;return a <code>String</code> where is stored a condition in text format
     */
    private String createQueryConditionsFromExpression(final Expression expression) {
        if (expression == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb = this.createQueryConditionsFromExpression(expression, sb);
        return sb.toString();
    }

    /**
     * Creates the condition string from a expression. The condition string is stored in the
     * <code>StringBuilder</code> param and the required values in a <code>Vector</code> param
     * @param expression a class that implements <code>Expression<code> interface
     * &#64;param values
     *            a list where the required values for the condition are stored
     * &#64;param sb
     *            a <code>StringBuilder</code> where the condition string is stored
     */
    private StringBuilder createQueryConditionsFromExpression(final Expression expression, final StringBuilder sb) {
        StringBuilder sbInner = sb;
        // Recursive
        Object lo = expression.getLeftOperand();
        final Object ro = expression.getRightOperand();
        final Object expressionOperator = expression.getOperator();

        sbInner.append("(");

        if (lo instanceof Field) {
            lo = this.getColumnName(lo.toString());
            if ((BasicOperator.LIKE_OP.equals(expressionOperator)
                    || BasicOperator.NOT_LIKE_OP.equals(expressionOperator)) && this.isUpperLike()) {
                lo = this.getUpperFunction() + "(" + lo + ")";
            } else if ((ro instanceof String) && this.isUpperStrings()) {
                lo = this.getUpperFunction() + "(" + lo + ")";
            }
            sbInner.append(lo);
            sbInner.append(" ");
            sbInner.append(expressionOperator);
            if (ro != null) {
                if (ro instanceof Expression) {
                    sbInner = this.createQueryConditionsFromExpression((Expression) ro, sbInner);
                } else if (ro instanceof Field) {
                    sbInner.append(" ");
                    sbInner.append(ro);
                } else {
                    if (BasicOperator.LIKE_OP.equals(expressionOperator)
                            || BasicOperator.NOT_LIKE_OP.equals(expressionOperator)) {
                        if (this.isUpperLike()) {
                            sbInner.append(this.getUpperFunction() + "(");
                            sbInner.append(" '");
                            sbInner.append(ro);
                            sbInner.append("' ");
                            sbInner.append(")");
                        } else {
                            sbInner.append(" '");
                            sbInner.append(ro);
                            sbInner.append("' ");
                        }
                    } else if ((ro instanceof String) && this.isUpperStrings()) {
                        sbInner.append(this.getUpperFunction() + "(");
                        sbInner.append(" '");
                        sbInner.append(ro);
                        sbInner.append("' ");
                        sbInner.append(")");
                    } else if (BasicOperator.IN_OP.equals(expressionOperator)) {
                        sbInner.append(" ");
                        StringBuilder sbInOp = new StringBuilder();
                        sbInOp = this.addValue(sbInOp, ro);
                        sbInner = this.addValueBetweenParenthesis(sbInner, sbInOp);
                        sbInner.append(" ");
                    } else {
                        sbInner.append(" ");
                        sbInner = this.addValue(sbInner, ro);
                        sbInner.append(" ");
                    }
                }
            }
        } else if (lo instanceof Expression) {
            sbInner = this.createQueryConditionsFromExpression((Expression) lo, sbInner);
            if (ro instanceof Expression) {
                sbInner.append(" ");
                sbInner.append(expressionOperator);
                sbInner.append(" ");
                sbInner = this.createQueryConditionsFromExpression((Expression) ro, sbInner);
            }
        }

        sbInner.append(")");

        return sbInner;

    }

    /**
     * Creates the condition string for a SQL Statement.
     * @param conditions a condition list
     * @param wildcards column list that can use wildcards
     * @param values vector where the value of each processed conditions is stored
     * @return
     */

    @Override
    public String createQueryConditions(final Map<?, ?> conditions, final List<String> wildcards) {
        // Separate the expressions
        Expression expression = null;
        if (conditions
            .containsKey(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)
                && (conditions.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY) instanceof Expression)) {
            expression = (Expression) conditions.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            conditions.remove(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
        } else if (conditions.containsKey(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
            conditions.remove(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
        }

        final String s = super.createQueryConditions(conditions, wildcards);

        // Now add the expressions
        final String sExp = this.createQueryConditionsFromExpression(expression);
        if ((sExp != null) && (s != null) && (s.length() > 0) && (sExp.length() > 0)) {
            return " ( " + s + "  ) AND " + sExp;
        } else if ((s != null) && (s.length() > 0)) {
            return s;
        } else if ((sExp != null) && (sExp.length() > 0)) {
            return sExp;
        } else {
            return s;
        }
    }

}
