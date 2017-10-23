package com.ontimize.jee.server.rest;

import java.util.HashMap;
import java.util.Map;

import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.db.SQLStatementBuilder.Operator;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;

public class BasicExpressionProcessor {

	protected static BasicExpressionProcessor	processor;

	protected static final String				OR				= BasicOperator.OR.trim();
	protected static final String				AND				= BasicOperator.AND.trim();
	protected static final String				OR_NOT			= BasicOperator.OR_NOT.trim();
	protected static final String				AND_NOT			= BasicOperator.AND_NOT.trim();
	protected static final String				LESS			= BasicOperator.LESS.trim();
	protected static final String				LESS_EQUAL		= BasicOperator.LESS_EQUAL.trim();
	protected static final String				EQUAL			= BasicOperator.EQUAL.trim();
	protected static final String				MORE_EQUAL		= BasicOperator.MORE_EQUAL.trim();
	protected static final String				MORE			= BasicOperator.MORE.trim();
	protected static final String				NULL			= BasicOperator.NULL.trim();
	protected static final String				NOT_EQUAL		= BasicOperator.NOT_EQUAL.trim();
	protected static final String				NOT_NULL		= BasicOperator.NOT_NULL.trim();
	protected static final String				LIKE			= BasicOperator.LIKE.trim();
	protected static final String				NOT_LIKE		= BasicOperator.NOT_LIKE.trim();
	protected static final String				IN				= SQLStatementBuilder.IN.trim();

	public static final String					LEFT_OPERAND	= "lop";
	public static final String					RIGHT_OPERAND	= "rop";
	public static final String					OPERATOR		= "op";

	public static BasicExpressionProcessor getInstance() {
		if (BasicExpressionProcessor.processor == null) {
			BasicExpressionProcessor.processor = new BasicExpressionProcessor();
		}
		return BasicExpressionProcessor.processor;
	}

	public BasicExpression processBasicEspression(Object value) throws OntimizeJEEException {
		return this.processBasicEspression(value, new HashMap<>());
	}

	public BasicExpression processBasicEspression(Object value, Map<?, ?> hSqlTypes) throws OntimizeJEEException {
		Object lo, ro;
		Operator operator;
		if (value instanceof Map) {
			Object lValue = ((Map) value).get(BasicExpressionProcessor.LEFT_OPERAND);
			if (lValue == null) {
				throw new OntimizeJEEException("'lop' isn't defined in basicexpression");
			}
			lo = this.processLeftOperand(lValue, hSqlTypes);

			Object operatorValue = ((Map) value).get(BasicExpressionProcessor.OPERATOR);
			if (operatorValue == null) {
				throw new OntimizeJEEException("'op' isn't defined in basicexpression");
			}
			operator = this.processOperator(operatorValue);

			Object rValue = ((Map) value).get(BasicExpressionProcessor.RIGHT_OPERAND);
			if (rValue == null) {
				if (operator.equals(BasicOperator.NULL_OP) || operator.equals(BasicOperator.NOT_NULL_OP)) {
					ro = null;
				} else {
					throw new OntimizeJEEException("'rop' isn't defined in basicexpression");
				}
			} else {
				if (lo instanceof BasicField) {
					ro = this.processRightOperand(rValue, (BasicField) lo, hSqlTypes);
				} else {
					ro = this.processRightOperand(rValue, null, hSqlTypes);
				}
			}
		} else {
			throw new OntimizeJEEException("value hasn't a BasicExpression format");
		}
		return new BasicExpression(lo, operator, ro);
	}

	protected Object processLeftOperand(Object lValue) throws OntimizeJEEException {
		return this.processLeftOperand(lValue, new HashMap<>());
	}

	protected Object processLeftOperand(Object lValue, Map<?, ?> hSqlTypes) throws OntimizeJEEException {
		if (lValue instanceof Map) {
			return this.processBasicEspression(lValue, hSqlTypes);
		} else {
			return new BasicField(lValue.toString());
		}
	}

	protected Object processRightOperand(Object rValue) throws OntimizeJEEException {
		return this.processRightOperand(rValue, null, new HashMap<>());
	}

	protected Object processRightOperand(Object rValue, BasicField bF, Map<?, ?> hSqlTypes) throws OntimizeJEEException {
		if (rValue instanceof Map) {
			return this.processBasicEspression(rValue, hSqlTypes);
		} else {
			if ((bF != null) && (hSqlTypes != null) && hSqlTypes.containsKey(bF.toString())) {
				int type = (Integer) hSqlTypes.get(bF.toString());
				return ParseUtilsExt.getValueForSQLType(rValue, type);
			} else {
				return rValue;
			}
		}
	}

	protected Operator processOperator(Object operatorValue) throws OntimizeJEEException {
		if (operatorValue instanceof String) {
			if (BasicExpressionProcessor.OR.equalsIgnoreCase((String) operatorValue)) {
				return BasicOperator.OR_OP;
			}

			if (BasicExpressionProcessor.AND.equalsIgnoreCase((String) operatorValue)) {
				return BasicOperator.AND_OP;
			}

			if (BasicExpressionProcessor.OR_NOT.equalsIgnoreCase((String) operatorValue)) {
				return BasicOperator.OR_NOT_OP;
			}

			if (BasicExpressionProcessor.AND_NOT.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.AND_NOT);
			}

			if (BasicExpressionProcessor.LESS.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.LESS);
			}

			if (BasicExpressionProcessor.LESS_EQUAL.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.LESS_EQUAL);
			}

			if (BasicExpressionProcessor.EQUAL.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.EQUAL);
			}

			if (BasicExpressionProcessor.MORE_EQUAL.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.MORE_EQUAL);
			}

			if (BasicExpressionProcessor.MORE.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.MORE);
			}

			if (BasicExpressionProcessor.NULL.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.NULL);
			}

			if (BasicExpressionProcessor.NOT_EQUAL.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.NOT_EQUAL);
			}

			if (BasicExpressionProcessor.NOT_NULL.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.NOT_NULL);
			}

			if (BasicExpressionProcessor.LIKE.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.LIKE);
			}

			if (BasicExpressionProcessor.NOT_LIKE.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(BasicOperator.NOT_LIKE);
			}

			if (BasicExpressionProcessor.IN.equalsIgnoreCase((String) operatorValue)) {
				return new BasicOperator(SQLStatementBuilder.IN);
			}

			throw new OntimizeJEEException("operator doesn't defined: " + operatorValue);
		} else {
			throw new OntimizeJEEException("operator must be a String instance");
		}
	}
}
