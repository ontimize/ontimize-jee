package com.ontimize.jee.common.db;

import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.db.SQLStatementBuilder.Expression;
import com.ontimize.jee.common.db.SQLStatementBuilder.ExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.SQLStatementBuilder.Field;
import com.ontimize.jee.common.db.SQLStatementBuilder.Operator;
import com.ontimize.jee.common.db.query.ParameterField;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ContainsExtendedSQLConditionValuesProcessor extends ExtendedSQLConditionValuesProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ContainsExtendedSQLConditionValuesProcessor.class);

    public static final String ANY_COLUMN = "QueryBuilderAnyColumn";

    @Override
    public String renderQueryConditionsFromExpression(Expression e) {
        StringBuilder sb = new StringBuilder();
        this.renderQueryConditionsFromExpression(e, sb);
        return sb.toString();
    }

    @Override
    public void renderQueryConditionsFromExpression(Expression e, StringBuilder sb) {
        if (e.getLeftOperand() instanceof Expression) {
            sb.append("(" + this.renderQueryConditionsFromExpression((Expression) e.getLeftOperand()));
            sb.append(" " + e.getOperator().toString() + " ");
            sb.append(this.renderQueryConditionsFromExpression((Expression) e.getRightOperand()) + ")");
        } else {
            // Simple expressions
            if (e.getOperator().toString().equals(ContainsOperator.CONTAINS_OP.toString())
                    || e.getOperator().toString().equals(ContainsOperator.NOT_CONTAINS_OP.toString())) {

                sb.append("(" + e.getLeftOperand().toString());
                sb.append(" " + e.getOperator().toString());
                if (e.getRightOperand() instanceof Field) {
                    sb.append(" " + e.getRightOperand());
                } else if (e.getRightOperand() instanceof String) {
                    sb.append(" '" + e.getRightOperand() + "')");
                } else if (e.getRightOperand() instanceof Expression) {
                    sb.append(" " + ContainsExtendedSQLConditionValuesProcessor
                        .createQueryConditionsExpress((Expression) e.getRightOperand()) + ")");
                }
            } else {
                sb.append(super.createQueryConditionsExpress(e));
            }
        }
    }


    public static String createQueryConditionsExpress(Expression e) {

        if (e == null) {
            return null;
        }
        if ((e.getLeftOperand() == null) || (e.getRightOperand() == null) || (e.getOperator() == null)) {
            return ExtendedSQLConditionValuesProcessor.createQueryConditionsExpress(e);
        }

        ContainsExtendedSQLConditionValuesProcessor sql = new ContainsExtendedSQLConditionValuesProcessor();

        return sql.renderQueryConditionsFromExpression(e);
    }

    public static Expression queryToStandard(Expression query, String[] cols, int[] types) {
        if (query == null) {
            return null;
        }
        Expression outputExpression = new BasicExpression(null, null, null);

        if (query.getLeftOperand() instanceof Expression) {
            outputExpression.setLeftOperand(ContainsExtendedSQLConditionValuesProcessor
                .queryToStandard((Expression) query.getLeftOperand(), cols, types));
            outputExpression.setOperator(query.getOperator());
            outputExpression.setRightOperand(ContainsExtendedSQLConditionValuesProcessor
                .queryToStandard((Expression) query.getRightOperand(), cols, types));
        } else {
            if (query.getOperator().toString().equals(ContainsOperator.CONTAINS_OP.toString()) || query.getOperator()
                .toString()
                .equals(ContainsOperator.NOT_CONTAINS_OP.toString())) {
                Operator op = BasicOperator.LIKE_OP;
                if (query.getOperator().toString().equals(ContainsOperator.NOT_CONTAINS_OP.toString())) {
                    op = BasicOperator.NOT_LIKE_OP;
                }

                String sValue = new String("");

                if (query.getRightOperand() instanceof ParameterField) {
                    if (((ParameterField) query.getRightOperand()).getValue() != null) {
                        sValue = (String) ((ParameterField) query.getRightOperand()).getValue();
                    }
                } else {
                    sValue = (String) query.getRightOperand();
                }

                List l = new ArrayList();
                for (int i = 0, a = cols.length; i < a; i++) {
                    // QueryBuilder.getVarchar()
                    if (!cols[i].equals(ANY_COLUMN) && (types[i] == 2)) {
                        l.add(new BasicExpression(new BasicField(cols[i]), op, "%" + sValue + "%"));
                    }
                }

                Expression orTotal = null;
                if (!l.isEmpty()) {
                    orTotal = (Expression) l.get(0);

                    Operator oper = BasicOperator.OR_OP;
                    if (query.getOperator().toString().equals(ContainsOperator.NOT_CONTAINS_OP.toString())) {
                        oper = BasicOperator.AND_OP;
                    }

                    for (int i = 1, a = l.size(); i < a; i++) {
                        orTotal = new BasicExpression(orTotal, oper, l.get(i));
                    }
                }
                return orTotal;

            } else {
                outputExpression.setLeftOperand(query.getLeftOperand());
                outputExpression.setOperator(query.getOperator());

                if (query.getOperator().toString().equals(BasicOperator.LIKE_OP.toString())
                        || query.getOperator().toString().equals(BasicOperator.NOT_LIKE_OP.toString())) {

                    Object o = query.getRightOperand();
                    String cons = "";
                    if (o instanceof String) {
                        cons = (String) o;
                    }
                    if (o instanceof ParameterField) {
                        if (((ParameterField) o).getValue() != null) {
                            cons = (String) ((ParameterField) o).getValue();
                        }
                    }

                    cons = cons.replace('*', '%');
                    cons = cons.replace('?', '_');

                    if ((cons.indexOf("%") == -1) && (cons.indexOf("_") == -1)) {
                        cons = "%" + cons + "%";
                    }
                    outputExpression.setRightOperand(cons);

                } else {
                    if (query.getRightOperand() instanceof ParameterField) {
                        outputExpression.setRightOperand(((ParameterField) query.getRightOperand()).getValue());
                    } else {
                        outputExpression.setRightOperand(query.getRightOperand());
                    }
                }
                return outputExpression;
            }

        }
        return outputExpression;
    }

    private static int[] convertType(java.util.List type) {
        int[] typeColumns = new int[type.size()];
        for (int i = 0; i < type.size(); i++) {
            String sType = (String) type.get(i);
            if ("Number".equalsIgnoreCase(sType)) {
                typeColumns[i] = 0;
            }
            if ("Date".equalsIgnoreCase(sType)) {
                typeColumns[i] = 1;
            }
            if ("String".equalsIgnoreCase(sType)) {
                typeColumns[i] = 2;
            }
        }
        return typeColumns;
    }

    public static Expression queryToStandard(Expression query, String entityName, List cols,
            EntityReferenceLocator locator) {
        try {
            Object entity = locator.getEntityReference(entityName);
            if (entity instanceof AdvancedQueryEntity) {

                AdvancedQueryEntity eAv = (AdvancedQueryEntity) entity;
                Map m = eAv.getColumnListForAvancedQuery(locator.getSessionId());

                ArrayList tips = new ArrayList();
                ArrayList colum = new ArrayList();
                Set setKeys = m.keySet();
                Iterator it = setKeys.iterator();
                while (it.hasNext()) {
                    Object c = it.next();
                    colum.add(c);
                    tips.add(m.get(c));
                }
                int[] iTypes = new int[tips.size()];
                iTypes = ContainsExtendedSQLConditionValuesProcessor.convertType(tips);

                int[] finalTypes = new int[cols.size()];
                for (int i = 0, a = cols.size(); i < a; i++) {
                    int k = colum.indexOf(cols.get(i));
                    if (k == -1) {
                        finalTypes[i] = 2;
                    } else {
                        finalTypes[i] = iTypes[k];
                    }
                }

                String[] sColumns = new String[cols.size()];
                for (int i = 0, a = cols.size(); i < a; i++) {
                    sColumns[i] = new String((String) cols.get(i));
                }

                return ContainsExtendedSQLConditionValuesProcessor.queryToStandard(query, sColumns, finalTypes);
            }
            return null;
        } catch (Exception e) {
            ContainsExtendedSQLConditionValuesProcessor.logger.error(null, e);
        }
        return null;
    }

}
