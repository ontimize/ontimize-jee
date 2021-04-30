package com.ontimize.jee.server.dao;

import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.db.SQLStatementBuilder.Expression;
import com.ontimize.jee.common.db.SQLStatementBuilder.ExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.SQLStatementBuilder.Field;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.tools.BasicExpressionTools;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.common.tools.MapTools;
import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.common.util.ParseTools;

@Component
@Lazy(value = true)
public class DownDateHelper implements ApplicationContextAware, IDownDateHelper {

    /**
     * Suffix added to downdate column to indicate that deprecated records must be included
     */
    public static final String INCLUDE = ".INCLUDE";

    /** The application context. */
    protected ApplicationContext applicationContext;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Gets the application context.
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public EntityResult downRecord(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String downDateColumn,
            Date downDate, Map<?, ?> keysValues) {
        Date downdateDate = ObjectTools.coalesce(downDate, (Date) keysValues.get(IDownDateHelper.DOWNDATE_DEFAULT_ATTR),
                new Date());
        return daoHelper.update(dao, EntityResultTools.keysvalues(downDateColumn, downdateDate), keysValues);
    }

    @Override
    public EntityResult upRecord(DefaultOntimizeDaoHelper daoHelper, IOntimizeDaoSupport dao, String downDateColumn,
            Map<?, ?> keysValues) {
        return daoHelper.update(dao, EntityResultTools.keysvalues(downDateColumn, new NullValue(Types.TIMESTAMP)),
                keysValues);
    }

    @Override
    public boolean checkDowndateQueryKeys(Map keysValues, String downDateColumn, String... daoKeyColumn) {
        boolean includeDowndateFilter = this.checkIncludeDowndateFilter(keysValues,
                downDateColumn + DownDateHelper.INCLUDE);
        if (includeDowndateFilter) {
            return false;
        }
        boolean hasDowndateFilter = this.hasDowndateFilter(keysValues, downDateColumn);
        boolean directRecordAccess = this.isDirectRecordAccess(keysValues, daoKeyColumn);
        if (!hasDowndateFilter && !directRecordAccess) {
            BasicExpression exp = (BasicExpression) keysValues.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            Expression expDownDate1 = new BasicExpression(new BasicField(downDateColumn), BasicOperator.NULL_OP, null);
            Expression expDownDate2 = new BasicExpression(new BasicField(downDateColumn), BasicOperator.MORE_OP,
                    new Date());
            BasicExpression expDownDate = new BasicExpression(expDownDate1, BasicOperator.OR_OP, expDownDate2);
            MapTools.safePut(keysValues, ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
                    BasicExpressionTools.combineExpression(exp, expDownDate));
            return true;
        }
        return hasDowndateFilter;
    }

    public boolean isDirectRecordAccess(Map keysValues, String... daoKeyColumns) {
        for (String daoKeyColumn : daoKeyColumns) {
            if (keysValues.containsKey(daoKeyColumn) && !(keysValues.get(daoKeyColumn) instanceof SearchValue)) {
                return true;
            }
        }
        if (keysValues.containsKey(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
            Expression exp = (Expression) keysValues.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            for (String daoKeyColumn : daoKeyColumns) {
                if (this.hasDowndateFilterByExpr(exp, daoKeyColumn)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasDowndateFilter(Map keysValues, String downDateColumn) {
        return keysValues.containsKey(downDateColumn) || this.hasDowndateFilterByEKUI(keysValues, downDateColumn);
    }

    protected boolean hasDowndateFilterByEKUI(Map keysValues, String downDateColumn) {
        if (keysValues.containsKey(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
            Expression exp = (Expression) keysValues.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            return this.hasDowndateFilterByExpr(exp, downDateColumn);

        }
        return false;
    }

    protected boolean hasDowndateFilterByExpr(Expression exp, String downDateColumn) {
        if (exp == null) {
            return false;
        }
        if (this.hasDowndateFilterByExprChild(exp.getLeftOperand(), downDateColumn)
                && exp.getOperator().equals(BasicOperator.EQUAL_OP)) {
            return true;
        }
        if (this.hasDowndateFilterByExprChild(exp.getRightOperand(), downDateColumn)
                && exp.getOperator().equals(BasicOperator.EQUAL_OP)) {
            return true;
        }
        return false;
    }

    protected boolean hasDowndateFilterByExprChild(Object operand, String downDateColumn) {
        if (operand instanceof Field) {
            if (ObjectTools.safeIsEquals(((Field) operand).toString(), downDateColumn)) {
                return true;
            }
        } else if (operand instanceof Expression) {
            return this.hasDowndateFilterByExpr((Expression) operand, downDateColumn);
        }

        return false;
    }

    public boolean checkIncludeDowndateFilter(Map keysValues, String checkIncludeColumn) {
        if (keysValues.containsKey(checkIncludeColumn)) {
            Object oChecked = keysValues.remove(checkIncludeColumn); // Remove this filter
            if (oChecked instanceof SearchValue) {
                oChecked = ((SearchValue) oChecked).getValue();
            }
            if (oChecked instanceof Boolean) {
                return ((Boolean) oChecked).booleanValue();
            } else {
                return ParseTools.getBoolean(String.valueOf(oChecked), false);
            }
        } else if (keysValues.containsKey(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY)) {
            Expression exp = (Expression) keysValues.get(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            CheckIncludePair res = this.checkIncludeDowndateFilterByExpr(exp, checkIncludeColumn);
            if ((res != null) && res.isAvailableToRemove()) {
                keysValues.remove(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY);
            }
            return res == null ? false : res.isEvaluation();
        }

        return false;
    }

    public CheckIncludePair checkIncludeDowndateFilterByExpr(Expression exp, String checkIncludeColumn) {
        if (exp == null) {
            return null;
        }
        if (exp.getLeftOperand() instanceof Field) {
            if (ObjectTools.safeIsEquals(((Field) exp.getLeftOperand()).toString(), checkIncludeColumn)) {
                boolean evaluation = this.parseBoolean(exp.getRightOperand());
                return new CheckIncludePair(true, evaluation);
            }
        } else if (exp.getLeftOperand() instanceof Expression) {
            CheckIncludePair resExpr = this.checkIncludeDowndateFilterByExpr((Expression) exp.getLeftOperand(),
                    checkIncludeColumn);
            if (resExpr != null) {
                if (resExpr.isAvailableToRemove()) {
                    // Remove this filter, composing again the expression, removing this filter
                    exp.setLeftOperand(((Expression) exp.getRightOperand()).getLeftOperand());
                    exp.setOperator(((Expression) exp.getRightOperand()).getOperator());
                    exp.setRightOperand(((Expression) exp.getRightOperand()).getRightOperand());
                }

                return new CheckIncludePair(false, resExpr.isEvaluation());
            }
            CheckIncludePair resExpr2 = this.checkIncludeDowndateFilterByExpr((Expression) exp.getRightOperand(),
                    checkIncludeColumn);
            if (resExpr2 != null) {
                if (resExpr2.isAvailableToRemove()) {
                    // Remove this filter, composing again the expression, removing this filter
                    Expression oldLeftExpr = (Expression) exp.getLeftOperand();
                    exp.setLeftOperand(oldLeftExpr.getLeftOperand());
                    exp.setOperator(oldLeftExpr.getOperator());
                    exp.setRightOperand(oldLeftExpr.getRightOperand());
                }
                return new CheckIncludePair(false, resExpr2.isEvaluation());
            }
        }
        return null;
    }

    public boolean parseBoolean(Object oChecked) {
        if (oChecked == null) {
            return false;
        }
        if (oChecked instanceof Boolean) {
            return ((Boolean) oChecked).booleanValue();
        } else {
            return ParseTools.getBoolean(String.valueOf(oChecked), false);
        }
    }

    protected class CheckIncludePair {

        protected boolean availableToRemove;

        protected boolean evaluation;

        public CheckIncludePair(boolean available, boolean evaluation) {
            this.availableToRemove = available;
            this.evaluation = evaluation;
        }

        public boolean isAvailableToRemove() {
            return this.availableToRemove;
        }

        public boolean isEvaluation() {
            return this.evaluation;
        }

    }

}
