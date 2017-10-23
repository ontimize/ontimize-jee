package com.ontimize.jee.common.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.gui.Form;
import com.ontimize.gui.SearchValue;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.util.ParseUtils;

/**
 * The Class BasicExpressionTools.
 */
public final class BasicExpressionTools {

	private static final Logger logger = LoggerFactory.getLogger(BasicExpressionTools.class);

	/**
	 * Instantiates a new basic expression tools.
	 */
	private BasicExpressionTools() {
		super();
	}

	/**
	 * Are equals.
	 *
	 * @param newExpr
	 *            the new expr
	 * @param oldExpr
	 *            the old expr
	 * @return true, if successful
	 */
	public static boolean areEquals(BasicExpression newExpr, BasicExpression oldExpr) {
		if (((newExpr == null) && (oldExpr != null)) || ((newExpr != null) && (oldExpr == null))) {
			return false;
		} else if (newExpr == null) {
			return true;
		}
		Object[] newOperands = new Object[] { newExpr.getLeftOperand(), newExpr.getRightOperand() };
		Object[] oldOperands = new Object[] { oldExpr.getLeftOperand(), oldExpr.getRightOperand() };
		if (!newExpr.getOperator().equals(oldExpr.getOperator())) {
			return false;
		}
		for (int i = 0; i < newOperands.length; i++) {
			Object nop = newOperands[i];
			Object oop = oldOperands[i];
			if (nop.getClass().equals(oop.getClass())) {
				if (nop instanceof BasicExpression) {
					if (!BasicExpressionTools.areEquals((BasicExpression) nop, (BasicExpression) oop)) {
						return false;
					}
				} else if (nop instanceof BasicField) {
					if (!ObjectTools.safeIsEquals(((BasicField) nop).toString(), ((BasicField) oop).toString())) {
						return false;
					}
				} else {
					if (!ObjectTools.safeIsEquals(nop, oop)) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Combine expression.
	 *
	 * @param exprs
	 *            the exprs
	 * @return the basic expression
	 */
	public static BasicExpression combineExpression(BasicExpression... exprs) {
		ArrayList<BasicExpression> join = new ArrayList<>();
		if (exprs != null) {
			for (BasicExpression expr : exprs) {
				if (expr != null) {
					join.add(expr);
				}
			}
		}
		BasicExpression fullExpr = null;
		for (BasicExpression newExpr : join) {
			if (fullExpr == null) {
				fullExpr = newExpr;
			} else {
				fullExpr = new BasicExpression(fullExpr, BasicOperator.AND_OP, newExpr);
			}
		}
		return fullExpr;
	}

	/**
	 * Convert search value.
	 *
	 * @param basicField
	 *            the basic field
	 * @param searchValue
	 *            the search value
	 * @return the basic expression
	 */
	public static BasicExpression convertSearchValue(BasicField basicField, SearchValue searchValue) {
		switch (searchValue.getCondition()) {
			case SearchValue.EQUAL:
				return new BasicExpression(basicField, BasicOperator.EQUAL_OP, searchValue.getValue());
			case SearchValue.IN:
				return new BasicExpression(basicField, BasicOperator.IN_OP, searchValue.getValue());
			case SearchValue.NOT_IN:
				return new BasicExpression(basicField, BasicOperator.NOT_IN_OP, searchValue.getValue());
			case SearchValue.LESS:
				return new BasicExpression(basicField, BasicOperator.LESS_OP, searchValue.getValue());
			case SearchValue.LESS_EQUAL:
				return new BasicExpression(basicField, BasicOperator.LESS_EQUAL_OP, searchValue.getValue());
			case SearchValue.MORE:
				return new BasicExpression(basicField, BasicOperator.MORE_OP, searchValue.getValue());
			case SearchValue.MORE_EQUAL:
				return new BasicExpression(basicField, BasicOperator.MORE_EQUAL_OP, searchValue.getValue());
			case SearchValue.NOT_EQUAL:
				return new BasicExpression(basicField, BasicOperator.NOT_EQUAL_OP, searchValue.getValue());
			case SearchValue.NOT_NULL:
				return new BasicExpression(basicField, BasicOperator.NOT_NULL_OP, searchValue.getValue());
			case SearchValue.NULL:
				return new BasicExpression(basicField, BasicOperator.NULL_OP, searchValue.getValue());
			case SearchValue.OR:
				return new BasicExpression(basicField, BasicOperator.IN_OP, searchValue.getValue());
			case SearchValue.BETWEEN:
				return new BasicExpression(new BasicExpression(basicField, BasicOperator.MORE_EQUAL_OP, ((List) searchValue.getValue()).get(0)), BasicOperator.AND_OP,
				        new BasicExpression(basicField, BasicOperator.LESS_EQUAL_OP, ((List) searchValue.getValue()).get(1)));
			case SearchValue.NOT_BETWEEN:
				return new BasicExpression(new BasicExpression(basicField, BasicOperator.LESS_OP, ((List) searchValue.getValue()).get(0)), BasicOperator.OR_OP,
				        new BasicExpression(basicField, BasicOperator.MORE_OP, ((List) searchValue.getValue()).get(1)));
			case SearchValue.EXISTS:
				throw new OntimizeJEERuntimeException("NOT_IMPLEMENTED");
			default:
				throw new OntimizeJEERuntimeException("INVALID_OP");
		}
	}

	/**
	 * Ensure to complete a BasicExpression with filters with another "susceptible" filters form another keys, depending on values at form. For each "otherKey" received, if tis
	 * value is not null at form will be considered to add as a filter.
	 *
	 * @param sourceExpr
	 *            the source expr
	 * @param form
	 *            the form
	 * @param otherKeys
	 *            List of possible keys to add to filter. It is accepted "String-with-attr", "SimpleFieldFilter-object" or "BetweenDateFilter-object"
	 * @return the basic expression
	 */
	public static BasicExpression completeExpresionFromKeys(BasicExpression sourceExpr, Form form, List<Object> otherKeys) {
		Map<Object, Object> kv = new HashMap<>();
		for (Object obj : otherKeys) {
			if (obj instanceof String) {
				// Direct filter from attr in form to db column
				Object value = form.getDataFieldValue((String) obj);
				if (value != null) {
					kv.put(obj, value);
				}
			} else if (obj instanceof SimpleFieldFilter) {
				// Simple filter from attr in form to another named db column
				Object value = form.getDataFieldValue(((SimpleFieldFilter) obj).getFormFieldAttr());
				if (value != null) {
					kv.put(((SimpleFieldFilter) obj).getFormFieldAttr(), value);
				}
			} else if (obj instanceof BetweenDateFilter) {
				// Simple filter from two attrs in form (since - to) to single db column
				Object value = form.getDataFieldValue(((BetweenDateFilter) obj).getSinceFormFieldAttr());
				if (value != null) {
					kv.put(((BetweenDateFilter) obj).getSinceFormFieldAttr(), value);
				}
				value = form.getDataFieldValue(((BetweenDateFilter) obj).getToFormFieldAttr());
				if (value != null) {
					kv.put(((BetweenDateFilter) obj).getToFormFieldAttr(), value);
				}
			}
		}
		return BasicExpressionTools.completeExpresionFromKeys(sourceExpr, kv, otherKeys);
	}

	public static BasicExpression completeExpresionFromKeys(BasicExpression sourceExpr, Map<Object, Object> kv, List<Object> otherKeys) {

		for (Object obj : otherKeys) {
			if (obj instanceof String) {
				// Direct filter from attr in form to db column
				Object value = kv.get(obj);
				if (value != null) {
					BasicField basicField = new BasicField((String) obj);
					BasicExpression fieldExpr = BasicExpressionTools.composeExpressionFromValue(basicField, value);
					sourceExpr = BasicExpressionTools.combineExpression(sourceExpr, fieldExpr);
				}
			} else if (obj instanceof SimpleFieldFilter) {
				// Simple filter from attr in form to another named db column
				Object value = kv.get(((SimpleFieldFilter) obj).getFormFieldAttr());
				if (value != null) {
					BasicField basicField = new BasicField(((SimpleFieldFilter) obj).getDbAttr());
					if (((SimpleFieldFilter) obj).isUseContains()) {
						value = "*" + value + "*";
					}
					BasicExpression fieldExpr = BasicExpressionTools.composeExpressionFromValue(basicField, value);
					sourceExpr = BasicExpressionTools.combineExpression(sourceExpr, fieldExpr);
				}
			} else if (obj instanceof BetweenDateFilter) {
				// Simple filter from two attrs in form (since - to) to single db column
				Object fromDate = BasicExpressionTools.getDate(kv.get(((BetweenDateFilter) obj).getSinceFormFieldAttr()));
				Object toDate = BasicExpressionTools.getDate(kv.get(((BetweenDateFilter) obj).getToFormFieldAttr()));
				BasicExpression fieldExpr = null;
				BasicField basicField = new BasicField(((BetweenDateFilter) obj).getDbAttr());
				if ((fromDate != null) && (toDate != null)) {
					fieldExpr = BasicExpressionTools.convertSearchValue(basicField,
					        new SearchValue(SearchValue.BETWEEN, new Vector<>(Arrays.asList(new Object[] { fromDate, toDate }))));
				} else if (fromDate != null) {
					fieldExpr = BasicExpressionTools.convertSearchValue(basicField, new SearchValue(SearchValue.MORE_EQUAL, fromDate));
				} else if (toDate != null) {
					fieldExpr = BasicExpressionTools.convertSearchValue(basicField, new SearchValue(SearchValue.LESS_EQUAL, toDate));
				}
				if (fieldExpr != null) {
					sourceExpr = BasicExpressionTools.combineExpression(sourceExpr, fieldExpr);
				} else if (((BetweenDateFilter) obj).getCheckFieldAttr() != null) {
					Object dataFieldValue = kv.get(((BetweenDateFilter) obj).getCheckFieldAttr());
					if (dataFieldValue != null) {
						boolean checked = false;
						try {
							checked = ((Boolean) dataFieldValue).booleanValue();
						} catch (Exception ex) {
							BasicExpressionTools.logger.debug("error getting boolean value, parsing...", ex);
							checked = ParseUtils.getBoolean(String.valueOf(dataFieldValue), false);
						}
						if (checked) {
							sourceExpr = BasicExpressionTools.combineExpression(sourceExpr,
							        BasicExpressionTools.convertSearchValue(basicField, new SearchValue(SearchValue.NOT_NULL, null)));
						} else {
							sourceExpr = BasicExpressionTools.combineExpression(sourceExpr,
							        BasicExpressionTools.convertSearchValue(basicField, new SearchValue(SearchValue.NULL, null)));
						}
					}
				}
			}
		}
		return sourceExpr;
	}

	/**
	 * Compose BasicExpression from input BasicField and according to value (can be a SearchValue, or have advanced character "*", ...)
	 *
	 * @param basicField
	 *            the basic field
	 * @param value
	 *            the value
	 * @return the basic expression
	 */
	public static BasicExpression composeExpressionFromValue(BasicField basicField, Object value) {
		BasicExpression fieldExpr;
		if (value == null) {
			fieldExpr = new BasicExpression(basicField, BasicOperator.NULL_OP, null);
		} else if (value instanceof SearchValue) {
			fieldExpr = BasicExpressionTools.convertSearchValue(basicField, (SearchValue) value);
		} else if ((value instanceof String) && ((String) value).contains("*")) {
			fieldExpr = new BasicExpression(basicField, BasicOperator.LIKE_OP, ((String) value).replaceAll(Pattern.quote("*"), "%"));
		} else {
			fieldExpr = new BasicExpression(basicField, BasicOperator.EQUAL_OP, value);
		}
		return fieldExpr;
	}

	/**
	 * Find condition for fields.
	 *
	 * @param expr
	 *            the expr
	 * @param fieldNames
	 *            the field names
	 * @return the basic expression
	 */
	public static BasicExpression findConditionForFields(BasicExpression expr, String... fieldNames) {
		if ((expr == null) || (fieldNames == null) || (fieldNames.length == 0)) {
			return null;
		}
		List<BasicExpression> validExpr = new ArrayList<>();
		Set<String> fields = new HashSet<>(Arrays.asList(fieldNames));
		BasicExpressionTools.findConditionForFields(expr, validExpr, fields);
		return BasicExpressionTools.combineExpression(validExpr.toArray(new BasicExpression[] {}));
	}

	/**
	 * Find condition for fields.
	 *
	 * @param expr
	 *            the expr
	 * @param validExpr
	 *            the valid expr
	 * @param fields
	 *            the fields
	 */
	private static void findConditionForFields(BasicExpression expr, List<BasicExpression> validExpr, Set<String> fields) {
		Object leftOperand = expr.getLeftOperand();
		Object rightOperand = expr.getRightOperand();
		Object[] toCheck = new Object[] { leftOperand, rightOperand };
		for (Object check : toCheck) {
			if (check instanceof BasicExpression) {
				BasicExpressionTools.findConditionForFields((BasicExpression) check, validExpr, fields);
			} else if (check instanceof BasicField) {
				if (fields.contains(((BasicField) check).toString())) {
					validExpr.add(expr);
				}
			}
		}
	}

	/**
	 * Copy without fields.
	 *
	 * @param expr
	 *            the expr
	 * @param fieldNames
	 *            the field names
	 * @return the basic expression
	 */
	public static BasicExpression copyWithoutFields(BasicExpression expr, String... fieldNames) {
		if (expr == null) {
			return null;
		}
		// BasicExpression res = new basicex
		Object newLeft = null;
		Object newRight = null;

		Set<String> fields = new HashSet<>(Arrays.asList(fieldNames));

		Object leftOperand = expr.getLeftOperand();
		if (leftOperand instanceof BasicExpression) {
			newLeft = BasicExpressionTools.copyWithoutFields((BasicExpression) leftOperand, fieldNames);
		} else if (leftOperand instanceof BasicField) {
			if (fields.contains(((BasicField) leftOperand).toString())) {
				newLeft = null;
			} else {
				newLeft = leftOperand;
			}
		} else {
			newLeft = leftOperand;
		}
		Object rightOperand = expr.getRightOperand();
		if (rightOperand instanceof BasicExpression) {
			newRight = BasicExpressionTools.copyWithoutFields((BasicExpression) rightOperand, fieldNames);
		} else if (rightOperand instanceof BasicField) {
			if (fields.contains(((BasicField) rightOperand).toString())) {
				newRight = null;
			} else {
				newRight = rightOperand;
			}
		} else {
			newRight = rightOperand;
		}
		if ((newRight != null) && (newLeft != null)) {
			return new BasicExpression(newLeft, expr.getOperator(), newRight);
		}
		if ((newLeft != null) && (newLeft instanceof BasicExpression)) {
			return (BasicExpression) newLeft;
		}
		if ((newRight != null) && (newRight instanceof BasicExpression)) {
			return (BasicExpression) newRight;
		}
		return null;
	}

	/**
	 * Rename field.
	 *
	 * @param expr
	 *            the expr
	 * @param fieldName
	 *            the field name
	 * @param newFieldName
	 *            the new field name
	 */
	public static void renameField(BasicExpression expr, String fieldName, String newFieldName) {
		if ((expr == null) || (fieldName == null) || (newFieldName == null)) {
			return;
		}
		Object leftOperand = expr.getLeftOperand();
		Object rightOperand = expr.getRightOperand();
		Object[] toCheck = new Object[] { leftOperand, rightOperand };
		for (Object check : toCheck) {
			if (check instanceof BasicExpression) {
				BasicExpressionTools.renameField((BasicExpression) check, fieldName, newFieldName);
			} else if (check instanceof BasicField) {
				if (fieldName.equals(((BasicField) check).toString())) {
					ReflectionTools.setFieldValue(check, "name", newFieldName);
				}
			}
		}
	}

	/**
	 * Contains field.
	 *
	 * @param expr
	 *            the expr
	 * @param fieldName
	 *            the field name
	 * @return true, if successful
	 */
	public static boolean containsField(BasicExpression expr, String fieldName) {
		if ((expr == null) || (fieldName == null)) {
			return false;
		}
		Object leftOperand = expr.getLeftOperand();
		Object rightOperand = expr.getRightOperand();
		Object[] toCheck = new Object[] { leftOperand, rightOperand };
		for (Object check : toCheck) {
			if (check instanceof BasicExpression) {
				if (BasicExpressionTools.containsField((BasicExpression) check, fieldName)) {
					return true;
				}
			} else if (check instanceof BasicField) {
				if (fieldName.equals(((BasicField) check).toString())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the date.
	 *
	 * @param value
	 *            the value
	 * @return the date
	 */
	public static Date getDate(Object value) {
		if ((value == null) || "".equals(value.toString())) {
			return null;
		}
		if (value instanceof Date) {
			return (Date) value;
		} else if (value instanceof SearchValue) {
			return (Date) ((SearchValue) value).getValue();
		}
		return (Date) value;
	}

	/**
	 * Special class that represent a filter for one DB column with one field at form.
	 */
	public static class SimpleFieldFilter {

		/** The form field attr. */
		protected String	formFieldAttr;

		/** The db attr. */
		protected String	dbAttr;

		/** The use contains. */
		protected boolean	useContains;	// Determine to use "*<value>*" -> Useful in clobs

		/**
		 * Instantiates a new simple field filter.
		 *
		 * @param formFieldAttr
		 *            the form field attr
		 * @param dbAttr
		 *            the db attr
		 */
		public SimpleFieldFilter(String formFieldAttr, String dbAttr) {
			this(formFieldAttr, dbAttr, false);
		}

		/**
		 * Instantiates a new simple field filter.
		 *
		 * @param formFieldAttr
		 *            the form field attr
		 * @param dbAttr
		 *            the db attr
		 * @param useContains
		 *            the use contains
		 */
		public SimpleFieldFilter(String formFieldAttr, String dbAttr, boolean useContains) {
			this.formFieldAttr = formFieldAttr;
			this.dbAttr = dbAttr;
			this.useContains = useContains;
		}

		/**
		 * Gets the form field attr.
		 *
		 * @return the form field attr
		 */
		public String getFormFieldAttr() {
			return this.formFieldAttr;
		}

		/**
		 * Gets the db attr.
		 *
		 * @return the db attr
		 */
		public String getDbAttr() {
			return this.dbAttr;
		}

		/**
		 * Checks if is use contains.
		 *
		 * @return true, if is use contains
		 */
		public boolean isUseContains() {
			return this.useContains;
		}
	}

	/**
	 * Special class that represent a date filter for one DB column but with two fields at form (since -- to). <br>Moreover, if configured with four parameters, the third means a
	 * CheckDataField that can be check or not to ensure force fitler by nullity.
	 */
	public static class BetweenDateFilter {

		/** The since form field attr. */
		protected String	sinceFormFieldAttr;

		/** The to form field attr. */
		protected String	toFormFieldAttr;

		/** The db attr. */
		protected String	dbAttr;

		/** The check field attr. */
		protected String	checkFieldAttr;

		/**
		 * Instantiates a new between date filter.
		 *
		 * @param sinceFormFieldAttr
		 *            the since form field attr
		 * @param toFormFieldAttr
		 *            the to form field attr
		 * @param dbAttr
		 *            the db attr
		 */
		public BetweenDateFilter(String sinceFormFieldAttr, String toFormFieldAttr, String dbAttr) {
			this(sinceFormFieldAttr, toFormFieldAttr, null, dbAttr);
		}

		/**
		 * Instantiates a new between date filter.
		 *
		 * @param sinceFormFieldAttr
		 *            the since form field attr
		 * @param toFormFieldAttr
		 *            the to form field attr
		 * @param checkFieldAttr
		 *            the check field attr
		 * @param dbAttr
		 *            the db attr
		 */
		public BetweenDateFilter(String sinceFormFieldAttr, String toFormFieldAttr, String checkFieldAttr, String dbAttr) {
			this.sinceFormFieldAttr = sinceFormFieldAttr;
			this.toFormFieldAttr = toFormFieldAttr;
			this.checkFieldAttr = checkFieldAttr;
			this.dbAttr = dbAttr;
		}

		/**
		 * Gets the since form field attr.
		 *
		 * @return the since form field attr
		 */
		public String getSinceFormFieldAttr() {
			return this.sinceFormFieldAttr;
		}

		/**
		 * Gets the to form field attr.
		 *
		 * @return the to form field attr
		 */
		public String getToFormFieldAttr() {
			return this.toFormFieldAttr;
		}

		/**
		 * Gets the check field attr.
		 *
		 * @return the check field attr
		 */
		public String getCheckFieldAttr() {
			return this.checkFieldAttr;
		}

		/**
		 * Gets the db attr.
		 *
		 * @return the db attr
		 */
		public String getDbAttr() {
			return this.dbAttr;
		}
	}

}
