/*
 *
 */
package com.ontimize.jee.common.tools;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResult;
import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.gui.SearchValue;
import com.ontimize.gui.field.ReferenceFieldAttribute;
import com.ontimize.gui.table.TableAttribute;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.report.TableSorter;

/**
 * Clase de utilidades para EntityResult.
 */
public final class EntityResultTools {

	private static final Logger	logger						= LoggerFactory.getLogger(EntityResultTools.class);
	/** The Constant MAX_IN_EXPRESSION_SUPPORT. */
	public static final int		MAX_IN_EXPRESSION_SUPPORT	= 1000;

	/**
	 * The Enum JoinType.
	 */
	public enum JoinType {

		/** The left. */
		LEFT, /** The inner. */
		INNER
	}

	private EntityResultTools() {
		super();
	}

	/**
	 * The Enum GroupType.
	 */
	public enum GroupType {

		/** The none. */
		NONE, /** The sum. */
		SUM, /** The min. */
		MIN, /** The max. */
		MAX, /** The avg. */
		AVG, /** The count */
		COUNT
	}

	public static class GroupTypeOperation {
		private final String	opColumn;
		private final GroupType	groupType;
		private final String	renameColumn;

		public String getOpColumn() {
			return this.opColumn;
		}

		public String getRenameColumn() {
			return this.renameColumn;
		}

		public GroupType getGroupType() {
			return this.groupType;
		}

		public GroupTypeOperation(String opColumn, GroupType groupType) {
			this.opColumn = opColumn;
			this.groupType = groupType;
			this.renameColumn = opColumn + "_" + groupType.name();
		}

		public GroupTypeOperation(String opColumn, String renameColumn, GroupType groupType) {
			this.opColumn = opColumn;
			this.groupType = groupType;
			this.renameColumn = renameColumn;
		}

	}

	/**
	 * Hace un join entre dos {@link EntityResult}. El join puede ser LEFT o INNER. En columnKeysA se indican las columnas por las que se hace el join
	 * (mismo nombre en los dos EntityResult)
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param columnKeysA
	 *            the column keys a
	 * @param joinType
	 *            the join type
	 * @return the entity result
	 */
	public static EntityResult doJoin(EntityResult a, EntityResult b, String[] columnKeysA, JoinType joinType) {
		return EntityResultTools.doJoin(a, b, columnKeysA, columnKeysA, joinType);
	}

	/**
	 * Hace un join entre dos {@link EntityResult}. El join puede ser LEFT o INNER. En columnKeysA y columnKeysB se indican las columnas por las que
	 * se hace el join (deben estar ordenadas de tal forma que a.columnKeysA[i] = b.columnKeysB[i])
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param columnKeysA
	 *            the column keys a
	 * @param columnKeysB
	 *            the column keys b
	 * @param joinType
	 *            the join type
	 * @return the entity result
	 */
	public static EntityResult doJoin(EntityResult a, EntityResult b, String[] columnKeysA, String[] columnKeysB, JoinType joinType) {
		EntityResult res = new EntityResult();
		Vector<Object> resColumnsA = new Vector<Object>(a.keySet());
		EntityResultTools.ensureCols(resColumnsA, columnKeysA);
		Vector<Object> resColumnsB = new Vector<Object>(b.keySet());
		EntityResultTools.ensureCols(resColumnsB, columnKeysB);
		Vector<Object> resColumns = new Vector<Object>();
		Vector<Object> resColumnsCommon = new Vector<Object>();
		resColumns.addAll(resColumnsA);
		for (int i = 0; i < resColumnsB.size(); i++) {
			Object col = resColumnsB.get(i);
			if (resColumns.contains(col)) {
				resColumnsCommon.add(col);
				resColumnsB.remove(i);
				resColumnsA.remove(col);
				i--;
			} else {
				resColumns.add(col);
			}
		}
		EntityResultTools.initEntityResult(res, resColumns);

		if (columnKeysA.length == 1) {
			res = EntityResultTools.doFastJoin(a, b, columnKeysA[0], columnKeysB[0], joinType, res, resColumnsA, resColumnsB, resColumns,
					resColumnsCommon);
		} else {
			int rcount = a.calculateRecordNumber();
			for (int i = 0; i < rcount; i++) {
				Hashtable<Object, Object> row = a.getRecordValues(i);
				EntityResultTools.doJoinForTable(res, resColumns, resColumnsA, resColumnsB, resColumnsCommon, row, b, columnKeysA, columnKeysB,
						joinType.equals(JoinType.INNER));
			}
			if (res.calculateRecordNumber() == 0) {
				res.clear();
			}
		}
		if (res.get(columnKeysA[0]) == null) {
			return res;
		}
		if (((Vector) res.get(columnKeysA[0])).size() == 0) {
			return new EntityResult();
		} else {
			return res;
		}
	}

	/**
	 * Anade a resColumns las columnas de cols que no contenga.
	 *
	 * @param resColumns
	 *            the res columns
	 * @param cols
	 *            the cols
	 */
	private static void ensureCols(Vector<Object> resColumns, String... cols) {
		for (String c : cols) {
			if (!resColumns.contains(c)) {
				resColumns.add(c);
			}
		}
	}

	/**
	 * Hace un join rapido entre dos entityResult por una columna usando el algoritmo FastQSort antes de hacer el join para acelerar el algoritmo.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param keyNameA
	 *            the key name a
	 * @param keyNameB
	 *            the key name b
	 * @param joinType
	 *            the join type
	 * @param res
	 *            the res
	 * @param resColumnsA
	 *            the res columns a
	 * @param resColumnsB
	 *            the res columns b
	 * @param resColumns
	 *            the res columns
	 * @param resColumnsCommon
	 *            the res columns common
	 * @return the entity result
	 */
	private static EntityResult doFastJoin(EntityResult a, EntityResult b, String keyNameA, String keyNameB, JoinType joinType, EntityResult res,
			Vector<Object> resColumnsA, Vector<Object> resColumnsB, Vector<Object> resColumns, Vector<Object> resColumnsCommon) {

		Object[] keysSortedA = null;
		int[] indexesA = null;
		if (!a.isEmpty()) {
			if (a.get(keyNameA) == null) {
				// No viene la clave --> no hay join
				if (JoinType.INNER == joinType) {
					return new EntityResult();
				} else {
					return a;
				}
			}
			keysSortedA = ((Vector) a.get(keyNameA)).toArray();
			indexesA = FastQSortAlgorithm.sort(keysSortedA);
		} else {
			keysSortedA = new Object[0];
			indexesA = new int[0];
		}

		Object[] keysSortedB = null;
		int[] indexesB = null;
		if (!b.isEmpty()) {
			keysSortedB = ((Vector) b.get(keyNameB)).toArray();
			indexesB = FastQSortAlgorithm.sort(keysSortedB);
		} else {
			keysSortedB = new Object[0];
			indexesB = new int[0];
		}

		int searchIndexB = 0;
		int resIndex = 0;

		for (int i = 0; i < indexesA.length; i++) {
			int found = 0;
			if (keysSortedA[i] != null) {
				for (; searchIndexB < keysSortedB.length; searchIndexB++) {
					if (keysSortedB[searchIndexB] != null) {
						int compareResult = ((Comparable) keysSortedA[i]).compareTo(keysSortedB[searchIndexB]);
						if (compareResult == 0) {
							found++;
							// FOUND
							for (Object col : resColumnsA) {
								try {
									((Vector) res.get(col)).add(resIndex, ((Vector) a.get(col)).get(indexesA[i]));
								} catch (Exception e) {
									EntityResultTools.logger.error(null, e);
								}
							}
							for (Object col : resColumnsB) {
								((Vector) res.get(col)).add(resIndex, ((Vector) b.get(col)).get(indexesB[searchIndexB]));
							}
							for (Object col : resColumnsCommon) {
								((Vector) res.get(col)).add(resIndex, ((Vector) b.get(col)).get(indexesB[searchIndexB]));
							}
							resIndex++;
						} else if (compareResult < 0) {
							if ((found > 0) && (i < (indexesA.length - 1)) && (keysSortedA[i].equals(keysSortedA[i + 1]))) {
								searchIndexB -= found;
							}
							break;
						}
					}
				}
			}
			// If left join remain A
			if ((found == 0) && joinType.equals(JoinType.LEFT)) {
				for (Object col : resColumnsA) {
					((Vector) res.get(col)).add(resIndex, ((Vector) a.get(col)).get(indexesA[i]));
				}
				for (Object col : resColumnsCommon) {
					((Vector) res.get(col)).add(resIndex, ((Vector) a.get(col)).get(indexesA[i]));
				}
				for (Object col : resColumnsB) {
					((Vector) res.get(col)).add(resIndex, null);
				}
				resIndex++;
			} else if ((found > 0) && (searchIndexB == keysSortedB.length) && (i < (indexesA.length - 1)) && (keysSortedA[i]
					.equals(keysSortedA[i + 1]))) {
				searchIndexB -= found;
			}
		}
		return res;
	}

	/**
	 * Do join for table.
	 *
	 * @param res
	 *            the res
	 * @param resColumns
	 *            the res columns
	 * @param resColumnsA
	 *            the res columns a
	 * @param resColumnsB
	 *            the res columns b
	 * @param resColumnsCommon
	 *            the res columns common
	 * @param rowA
	 *            the row a
	 * @param b
	 *            the b
	 * @param columnKeysA
	 *            the column keys a
	 * @param columnKeysB
	 *            the column keys b
	 * @param onlyInnerJoin
	 *            the only inner join
	 */
	private static void doJoinForTable(EntityResult res, Vector<Object> resColumns, Vector<Object> resColumnsA, Vector<Object> resColumnsB,
			Vector<Object> resColumnsCommon, Hashtable<Object, Object> rowA, EntityResult b, String[] columnKeysA, String[] columnKeysB,
			boolean onlyInnerJoin) {
		int rcount = b.calculateRecordNumber();
		boolean match = false;
		int index = res.calculateRecordNumber();
		for (int i = 0; i < rcount; i++) {
			Hashtable<Object, Object> testRow = b.getRecordValues(i);
			if (EntityResultTools.check(rowA, testRow, columnKeysA, columnKeysB)) {
				match = true;
				for (Object col : resColumnsA) {
					try {
						((Vector) res.get(col)).add(index, rowA.get(col));
					} catch (Exception e) {
						EntityResultTools.logger.error(null, e);
					}
				}
				for (Object col : resColumnsB) {
					((Vector) res.get(col)).add(index, testRow.get(col));
				}
				for (Object col : resColumnsCommon) {
					((Vector) res.get(col)).add(index, testRow.get(col));
				}
				index++;
			}
		}
		if ((!match) && (!onlyInnerJoin)) {
			for (Object col : resColumnsA) {
				((Vector) res.get(col)).add(index, rowA.get(col));
			}
			for (Object col : resColumnsCommon) {
				((Vector) res.get(col)).add(index, rowA.get(col));
			}
			for (Object col : resColumnsB) {
				((Vector) res.get(col)).add(index, null);
			}
		}
	}

	/**
	 * Comprueba si las columnas son valores para las keys pasadas con iguales.
	 *
	 * @param ha
	 *            the ha
	 * @param hb
	 *            the hb
	 * @param columnKeysA
	 *            the column keys a
	 * @param columnKeysB
	 *            the column keys b
	 * @return true, if successful
	 */
	public static boolean check(Hashtable<Object, Object> ha, Hashtable<Object, Object> hb, String[] columnKeysA, String[] columnKeysB) {
		for (int i = 0; i < columnKeysA.length; i++) {
			Object oa = ha.get(columnKeysA[i]);
			Object ob = hb.get(columnKeysB[i]);
			if ((oa == null) && (ob != null)) {
				return false;
			} else if ((oa != null) && (!oa.equals(ob))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Inicializa un {@link EntityResult} con las columnas y la longitud indicada.
	 *
	 * @param res
	 *            the res
	 * @param columns
	 *            the columns
	 * @param length
	 *            the length
	 */
	public static void initEntityResult(EntityResult res, List<?> columns, int length) {
		for (Object col : columns) {
			res.put(col, new Vector<Object>(length > 0 ? length : 10));
		}
	}

	/**
	 * Inicializa un {@link EntityResult} con las columnas indicadas.
	 *
	 * @param res
	 *            the res
	 * @param columns
	 *            the columns
	 */
	public static void initEntityResult(EntityResult res, String... columns) {
		EntityResultTools.initEntityResult(res, Arrays.asList(columns));
	}

	/**
	 * Inicializa un {@link EntityResult} con las columnas indicadas.
	 *
	 * @param res
	 *            the res
	 * @param asList
	 *            the as list
	 */
	public static void initEntityResult(EntityResult res, List<?> asList) {
		EntityResultTools.initEntityResult(res, asList, 0);
	}

	/**
	 * Agrupa los valores de un {@link EntityResult}. Se pueden indicar funciones de agregado
	 *
	 * @param er
	 *            the entity result
	 * @param groupColumns
	 *            the group columns
	 * @param groupOperations
	 *            the GroupTypeOperation
	 * @return the entity result
	 * @throws Exception
	 *             the exception
	 */
	public static EntityResult doGroup(EntityResult er, String[] groupColumns, GroupTypeOperation... groupOperations) throws Exception {
		if (groupColumns.length <= 0) {
			throw new Exception("GroupColumns are mandatory");
		}

		if (er.isEmpty()) {
			return er;
		}

		if (groupColumns.length == 1) {
			return EntityResultTools.doFastGroup(er, groupColumns[0], groupOperations);
		} else {
			return EntityResultTools.doSlowGroup(er, groupColumns, groupOperations);
		}
	}

	/**
	 * Do slow group.
	 *
	 * @param er
	 *            the entity result
	 * @param groupColumn
	 *            the group column
	 * @param GroupTypeOperation
	 *            the group type operations
	 * @return the entity result
	 * @throws Exception
	 *             the exception
	 */
	private static EntityResult doSlowGroup(EntityResult er, String[] groupColumns, GroupTypeOperation... groupOperations) throws Exception {
		Vector<Group> groups = new Vector<Group>();

		int rcount = er.calculateRecordNumber();
		for (int i = 0; i < rcount; i++) {
			Hashtable recordValues = er.getRecordValues(i);
			Group group = EntityResultTools.checkGroup(groups, recordValues);
			if (group != null) {
				// Add values
				for (GroupTypeOperation groupOperation : groupOperations) {
					String opColumn = groupOperation.getOpColumn();
					group.addValue(recordValues.get(opColumn));
				}
			} else {
				// Add keys and values
				Hashtable ks = new Hashtable();
				for (String s : groupColumns) {
					Object value = recordValues.get(s);
					if (value == null) {
						value = "WARN-no method";
						// throw new Exception("GroupColumn \"" + s + "\" is null. It is not supported");
					}
					ks.put(s, value);
				}
				Group newGroup = new Group(ks);
				for (GroupTypeOperation groupOperation : groupOperations) {
					String opColumn = groupOperation.getOpColumn();
					newGroup.addValue(recordValues.get(opColumn));
				}
				groups.add(newGroup);
			}

		}

		EntityResult res = new EntityResult();
		Vector<Object> resultColumns = new Vector<Object>(Arrays.asList(groupColumns));
		for (GroupTypeOperation groupOperation : groupOperations) {
			String renameColumn = groupOperation.getRenameColumn();
			resultColumns.add(renameColumn);
		}
		EntityResultTools.initEntityResult(res, resultColumns);

		for (Group g : groups) {
			Hashtable record = new Hashtable();
			// Group columns
			for (String s : groupColumns) {
				record.put(s, g.getKeys().get(s));
			}

			// Grouped column
			int length = groupOperations.length;
			for (int i = 0; i <= (length - 1); i++) {
				GroupTypeOperation groupTypeOperation = groupOperations[i];
				GroupType groupType = groupTypeOperation.getGroupType();
				String renameColumn = groupTypeOperation.getRenameColumn();
				// Select correct values
				Vector values = g.getValues();
				Vector opValues = new Vector<>();
				for (int y = i; y <= (values.size() - 1); y += length) {
					opValues.add(values.get(y));
				}

				Object value = EntityResultTools.parseGroupFunction(groupType, opValues);
				if (value != null) {
					record.put(renameColumn, value);
				}

			}
			res.addRecord(record);
		}
		return res;
	}

	/**
	 * Do fast group.
	 *
	 * @param er
	 *            the entity result
	 * @param groupColumn
	 *            the group column
	 * @param GroupTypeOperation
	 *            the group type operations
	 * @return the entity result
	 */
	private static EntityResult doFastGroup(EntityResult er, String groupColumn, GroupTypeOperation... groupOperations) {
		boolean hasOp = false;
		List<String> opColumns = new ArrayList<String>();
		if (groupOperations != null) {
			hasOp = true;
			for (GroupTypeOperation groupOperation : groupOperations) {
				String opColumn = groupOperation.getOpColumn();
				String opName = groupOperation.getGroupType().name();
				opColumns.add(opColumn + "_" + opName);
				if (er.get(opColumn) == null) {
					hasOp = false;
				}
			}
		}
		EntityResult res = new EntityResult();
		opColumns.add(groupColumn);
		EntityResultTools.initEntityResult(res, opColumns);
		Object[] keysSorted = ((Vector) er.get(groupColumn)).toArray();
		int[] indexes = FastQSortAlgorithm.sort(keysSorted);
		Object currentKey = null;
		int resIndex = 0;

		int counter = 0;

		Object currentMinOp = null;
		Object currentMaxOp = null;
		Object currentAvgOp = null;
		Object currentSumOp = null;

		Vector vectorGroupColumn = (Vector) res.get(groupColumn);
		for (int i = 0; i < keysSorted.length; i++) {
			if ((keysSorted[i] == null) || !keysSorted[i].equals(currentKey)) {
				// cambio de grupo
				if (currentKey != null) {
					resIndex = EntityResultTools.refactor(groupColumn, res, currentKey, resIndex, counter, currentMinOp, currentMaxOp, currentAvgOp,
							currentSumOp, vectorGroupColumn, groupOperations);
				}
				currentKey = keysSorted[i];

				// currentOp = null;
				currentMinOp = null;
				currentMaxOp = null;
				currentAvgOp = null;
				currentSumOp = null;
				counter = 0;
			}
			counter++;
			if (hasOp) {
				for (GroupTypeOperation groupOperation : groupOperations) {
					Object newOp = ((Vector) er.get(groupOperation.getOpColumn())).get(indexes[i]);
					GroupType groupType = groupOperation.getGroupType();
					if (groupType != null) {
						switch (groupType) {
							case NONE:
								currentSumOp = null;
								currentMinOp = null;
								currentMaxOp = null;
								currentAvgOp = null;
								counter = 0;
								break;
							case AVG:
								Number converted = ((newOp instanceof Number) || (newOp == null)) ? (Number) newOp : new Double((String) newOp);
								if (currentAvgOp == null) {
									currentAvgOp = converted;
								} else if (converted != null) {
									double c = ((Number) currentAvgOp).doubleValue();
									double n = converted.doubleValue();
									currentAvgOp = new Double((n + (c * (counter - 1))) / counter);
								}
								break;
							case MAX:
								converted = ((newOp instanceof Number) || (newOp == null)) ? (Number) newOp : new Double((String) newOp);
								if (currentMaxOp == null) {
									currentMaxOp = converted;
								} else if (converted != null) {
									double c = ((Number) currentMaxOp).doubleValue();
									double n = converted.doubleValue();
									currentMaxOp = (n > c) ? converted : currentMaxOp;
								}
								break;
							case MIN:
								converted = ((newOp instanceof Number) || (newOp == null)) ? (Number) newOp : new Double((String) newOp);
								if (currentMinOp == null) {
									currentMinOp = converted;
								} else if (converted != null) {
									double c = ((Number) currentMinOp).doubleValue();
									double n = converted.doubleValue();
									currentMinOp = (n < c) ? converted : currentMinOp;
								}
								break;
							case SUM:
								converted = ((newOp instanceof Number) || (newOp == null)) ? (Number) newOp : new Double((String) newOp);
								if (currentSumOp == null) {
									currentSumOp = converted;
								} else if (converted != null) {
									double c = ((Number) currentSumOp).doubleValue();
									double n = converted.doubleValue();
									currentSumOp = new Double(c + n);
								}
								break;
							default:
								break;
						}
					}
				}
			}
		}
		if (currentKey != null) {
			resIndex = EntityResultTools.refactor(groupColumn, res, currentKey, resIndex, counter, currentMinOp, currentMaxOp, currentAvgOp,
					currentSumOp, vectorGroupColumn, groupOperations);
		}

		if (groupOperations != null) {
			for (GroupTypeOperation groupOperation : groupOperations) {
				String opColumn = groupOperation.getOpColumn() + "_" + groupOperation.getGroupType().name();
				String renameColumn = groupOperation.getRenameColumn();
				if (!groupColumn.equals(opColumn)) {
					GroupType groupType = groupOperation.getGroupType();
					if (groupType != null) {
						EntityResultTools.renameColumn(res, opColumn, renameColumn);
					}
				}
			}
		}
		return res;
	}

	private static int refactor(String groupColumn, EntityResult res, Object currentKey, int resIndex, int counter, Object currentMinOp,
			Object currentMaxOp, Object currentAvgOp, Object currentSumOp, Vector vectorGroupColumn, GroupTypeOperation... groupOperations) {
		vectorGroupColumn.add(resIndex, currentKey);

		for (GroupTypeOperation groupOperation : groupOperations) {
			String opColumn = groupOperation.getOpColumn() + "_" + groupOperation.getGroupType().name();
			if (!groupColumn.equals(opColumn)) {
				Object currentOp = null;
				switch (groupOperation.getGroupType()) {
					case NONE:
						break;
					case COUNT:
						currentOp = counter;
						break;
					case AVG:
						currentOp = currentAvgOp;
						break;
					case MAX:
						currentOp = currentMaxOp;
						break;
					case MIN:
						currentOp = currentMinOp;
						break;
					case SUM:
						currentOp = currentSumOp;
						break;
					default:
						break;
				}
				((Vector) res.get(opColumn)).add(resIndex, currentOp);
			}
		}
		resIndex++;
		return resIndex;
	}

	/**
	 * Parses the group function.
	 *
	 * @param groupType
	 *            the group type
	 * @param values
	 *            the values
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	private static Object parseGroupFunction(GroupType groupType, List<?> values) throws OntimizeJEEException {
		// TODO Separar cada funcion (interfaz IGroupFnction)
		// TODO No necesariamente necesitamos un double, deberia ser del tipo de
		// dato que nos llega
		if (groupType == GroupType.NONE) {
			return null;
		} else if (groupType == GroupType.SUM) {
			BigDecimal finalValue = new BigDecimal(0.0d);
			boolean someValue = false;
			for (Object o : values) {
				if (o != null) {
					if (!(o instanceof Number)) {
						throw new OntimizeJEEException("Not a number in sum operation");
					}
					finalValue = finalValue.add(o instanceof BigDecimal ? ((BigDecimal) o) : new BigDecimal(o.toString()));
					someValue = true;
				}
			}
			return someValue ? finalValue : null;
		} else if (groupType == GroupType.MAX) {
			BigDecimal finalValue = new BigDecimal(-Double.MAX_VALUE);
			boolean someValue = false;
			for (Object o : values) {
				if (o == null) {
					continue;
				}
				try {
					if (!(o instanceof Number)) {
						o = Double.parseDouble(o.toString());
					}
				} catch (Exception e) {
					throw new OntimizeJEEException("Required numeric column to group.");
				}

				finalValue = ((Number) o).doubleValue() > finalValue.doubleValue() ? new BigDecimal(o.toString()) : finalValue;
				someValue = true;
			}
			return someValue ? finalValue : null;
		} else if (groupType == GroupType.MIN) {
			BigDecimal finalValue = new BigDecimal(+Double.MAX_VALUE);
			boolean someValue = false;
			for (Object o : values) {
				if (o == null) {
					continue;
				}
				try {
					if ((o != null) && !(o instanceof Number)) {
						o = Double.parseDouble(o.toString());
					}
				} catch (Exception e) {
					throw new OntimizeJEEException("Required numeric column to group.");
				}

				finalValue = ((Number) o).doubleValue() < finalValue.doubleValue() ? new BigDecimal(o.toString()) : finalValue;
				someValue = true;
			}
			return someValue ? finalValue : null;
		} else if (groupType == GroupType.AVG) {
			BigDecimal finalValue = new BigDecimal(0.0d);
			boolean someValue = false;
			for (Object o : values) {
				if (o != null) {
					if (!(o instanceof Number)) {
						throw new OntimizeJEEException("Not a number in sum operation");
					}
					finalValue = finalValue.add(o instanceof BigDecimal ? ((BigDecimal) o) : new BigDecimal(o.toString()));
					someValue = true;
				}
			}
			return someValue ? finalValue.divide(new BigDecimal(values.size()), new MathContext(10, RoundingMode.UP)) : null;
		} else if (groupType == GroupType.COUNT) {
			return new BigDecimal(values.size());
		} else {
			throw new OntimizeJEEException("Unsupported group funtion \"" + groupType.toString() + " \".");
		}
	}

	/**
	 * Cambia el nombre de una columna del {@link EntityResult}.
	 *
	 * @param er
	 *            the er
	 * @param fromColumn
	 *            the from column
	 * @param toColumn
	 *            the to column
	 */
	public static void renameColumn(EntityResult er, String fromColumn, String toColumn) {
		Object toRename = er.remove(fromColumn);
		if (toRename != null) {
			er.put(toColumn, toRename);
		}
	}

	/**
	 * Check group.
	 *
	 * @param groups
	 *            the groups
	 * @param recordValues
	 *            the record values
	 * @return the group
	 */
	private static Group checkGroup(Vector<Group> groups, Hashtable recordValues) {
		for (Group g : groups) {
			Hashtable groupKeys = g.getKeys();
			Enumeration keys = groupKeys.keys();
			boolean isEqGroup = true;

			while (keys.hasMoreElements()) {
				Object curKey = keys.nextElement();

				if (groupKeys.get(curKey) instanceof String) {
					if (!((String) groupKeys.get(curKey)).equals(recordValues.get(curKey))) {
						isEqGroup = false;
						break;
					}
				} else {
					if (!groupKeys.get(curKey).equals(recordValues.get(curKey))) {
						isEqGroup = false;
						break;
					}
				}
			}
			if (isEqGroup) {
				return g;
			}
		}

		return null;
	}

	/**
	 * Do sort.
	 *
	 * @param res
	 *            the res
	 * @param cols
	 *            the cols
	 * @return the entity result
	 */
	public static EntityResult doSort(EntityResult res, String... cols) {
		if (cols.length == 1) {
			return EntityResultTools.doFastSort(res, cols[0]);
		} else {
			return EntityResultTools.doSlowSort(res, cols);
		}

	}

	/**
	 * Do fast sort.
	 *
	 * @param a
	 *            the a
	 * @param col
	 *            the col
	 * @return the entity result
	 */
	private static EntityResult doFastSort(EntityResult a, String col) {
		if (a.calculateRecordNumber() < 2) {
			return a;
		}
		Object[] keysSorted = ((Vector) a.get(col)).toArray();
		int[] indexes = FastQSortAlgorithm.sort(keysSorted);
		Vector cols = new Vector(a.keySet());
		EntityResult res = new EntityResult();
		EntityResultTools.initEntityResult(res, cols, indexes.length);
		for (Object key : cols) {
			Vector vOrig = (Vector) a.get(key);
			Vector vDest = (Vector) res.get(key);
			for (int i = 0; i < indexes.length; i++) {
				vDest.add(i, vOrig.get(indexes[i]));
			}
		}
		return res;
	}

	/**
	 * Do slow sort.
	 *
	 * @param res
	 *            the res
	 * @param cols
	 *            the cols
	 * @return the entity result
	 */
	protected static EntityResult doSlowSort(EntityResult res, String... cols) {
		if (res != null) {
			TableModel model = com.ontimize.db.EntityResultUtils.createTableModel(res, new Vector(res.keySet()), false, false, false);
			TableSorter sorter = new TableSorter(model) {
				@Override
				public int compareRowsByColumn(int row1, int row2, int column) {
					Class type = this.model.getColumnClass(column);
					TableModel data = this.model;
					if (type == java.lang.String.class) {
						String s1 = (String) data.getValueAt(row1, column);
						String s2 = (String) data.getValueAt(row2, column);
						if (s1 == null) {
							return -1;
						}
						if (s2 == null) {
							return 1;
						}
						return s1.compareToIgnoreCase(s2);
					} else {
						return super.compareRowsByColumn(row1, row2, column);
					}
				}
			};
			for (String column : cols) {
				sorter.sortByColumn(EntityResultTools.getColumnIndex(sorter, column));
			}
			return EntityResultTools.getOrderedEntityResult(sorter, res);
		} else {
			return res;
		}

	}

	/**
	 * Gets the ordered entity result.
	 *
	 * @param sorter
	 *            the sorter
	 * @param er
	 *            the er
	 * @return the ordered entity result
	 */
	protected static EntityResult getOrderedEntityResult(TableSorter sorter, EntityResult er) {
		EntityResult res = new EntityResult();
		EntityResultTools.initEntityResult(res, new Vector(er.keySet()));
		int j = 0;
		for (int i : sorter.getIndexes()) {
			res.addRecord(er.getRecordValues(i), j);
			j++;
		}
		return res;
	}

	/**
	 * Prints the result.
	 *
	 * @param res
	 *            the res
	 */
	private static String printResult(EntityResult res) {
		StringBuilder sb = new StringBuilder();
		if ((res == null) || res.isEmpty()) {
			sb.append("Result empty.");
			return sb.toString();
		}
		int numR = res.calculateRecordNumber();
		Set keySet = res.keySet();
		sb.append("\n\n------------------- Printing result -----------------");
		for (Object key : keySet) {
			sb.append(key).append(StringTools.TAB);
		}
		sb.append(StringTools.WEOL);
		for (int i = 0; i < numR; i++) {
			for (Object key : keySet) {
				sb.append(((Vector) res.get(key)).get(i)).append(StringTools.TAB);
			}
			sb.append(StringTools.WEOL);
		}
		return sb.toString();
	}

	/**
	 * Gets the column index.
	 *
	 * @param model
	 *            the model
	 * @param column
	 *            the column
	 * @return the column index
	 */
	protected static int getColumnIndex(TableModel model, String column) {
		for (int i = 0; i < model.getColumnCount(); i++) {
			if (column.equals(model.getColumnName(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * The Class Group.
	 */
	static class Group {

		/** The keys. */
		protected Hashtable	keys;

		/** The values. */
		protected Vector	values;

		/**
		 * Instantiates a new group.
		 *
		 * @param keys
		 *            the keys
		 * @param values
		 *            the values
		 */
		public Group(Hashtable keys) {
			this.keys = keys;
		}

		/**
		 * Instantiates a new group.
		 *
		 * @param keys
		 *            the keys
		 * @param values
		 *            the values
		 */
		public Group(Hashtable keys, Vector values) {
			this.keys = keys;
			this.values = values;
		}

		/**
		 * Instantiates a new group.
		 *
		 * @param keys
		 *            the keys
		 * @param value
		 *            the value
		 */
		public Group(Hashtable keys, Object value) {
			this.keys = keys;
			this.values = new Vector();
			this.values.add(value);
		}

		/**
		 * Gets the keys.
		 *
		 * @return the keys
		 */
		public Hashtable getKeys() {
			return this.keys;
		}

		/**
		 * Gets the values.
		 *
		 * @return the values
		 */
		public Vector getValues() {
			return this.values;
		}

		/**
		 * Adds the value.
		 *
		 * @param value
		 *            the value
		 */
		public void addValue(Object value) {
			if (this.values == null) {
				this.values = new Vector();
			}
			this.values.add(value);
		}
	}

	/**
	 * Imprime un entityResult.
	 *
	 * @param er
	 *            the er
	 * @return the string
	 */
	public static String toString(EntityResult er) {
		StringBuilder sb = new StringBuilder();
		if (er == null) {
			sb.append("EntityResult is NULL");
		} else {
			int nregs = er.calculateRecordNumber();
			sb.append("Total de registros: ").append(nregs).append("\r\n");
			// Primero las cabeceras
			List<?> keyList = new ArrayList<Object>(er.keySet());

			for (Object key : keyList) {
				sb.append(key).append("\t");
			}
			sb.append("\r\n");

			for (int i = 0; i < nregs; i++) {
				Hashtable recordValues = er.getRecordValues(i);
				for (Object key : keyList) {
					sb.append(recordValues.get(key)).append("\t");
				}
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Hace un UNION ALL de los {@link EntityResult} que se pasan como parametro. El {@link EntityResult} final tiene las columnas de todos los
	 *
	 * @param vRes
	 *            the v res
	 * @return the entity result {@link EntityResult} que se han pasado. En caso de que los {@link EntityResult} tengan las columnas con el mismo
	 *         identificador, se combinan.
	 */
	public static EntityResult doUnionAll(EntityResult... vRes) {
		if ((vRes == null) || (vRes.length == 0)) {
			EntityResult res = new EntityResult();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}

		EntityResult res = new EntityResult();
		res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		res.setCompressionThreshold(vRes[0].getCompressionThreshold());
		res.setType(EntityResult.DATA_RESULT);

		// primero sacamos la lista de columnas totales
		List<Object> columnList = EntityResultTools.getColumnList(vRes);
		EntityResultTools.initEntityResult(res, columnList);

		// Combinamos los datos
		int[] numregs = new int[vRes.length];
		for (int i = 0; i < vRes.length; i++) {
			numregs[i] = vRes[i].calculateRecordNumber();
		}
		for (Object ob : columnList) {
			Vector<Object> vtotal = (Vector<Object>) res.get(ob);
			int curIndex = 0;
			for (int i = 0; i < vRes.length; i++) {
				EntityResult er = vRes[i];
				Vector<Object> vpart = (Vector<Object>) er.get(ob);
				if (vpart == null) {
					Object[] tmp = new Object[numregs[i]];
					Arrays.fill(tmp, null);
					vtotal.addAll(curIndex, Arrays.asList(tmp));
				} else {
					vtotal.addAll(curIndex, vpart);
				}
				curIndex += numregs[i];
			}
		}
		return res;
	}

	/**
	 * Hace un UNION (elimina duplicados) de los {@link EntityResult} que se pasan como parametro. El {@link EntityResult} final tiene las columnas de
	 * todos los
	 *
	 * @param vRes
	 *            the v res
	 * @return the entity result {@link EntityResult} que se han pasado. En caso de que los {@link EntityResult} tengan las columnas con el mismo
	 *         identificador, se combinan.
	 */
	public static EntityResult doUnion(EntityResult... vRes) {
		EntityResult doUnionAll = EntityResultTools.doUnionAll(vRes);

		// Now clean duplicated entries
		EntityResult newResult = new EntityResult();
		newResult.setCode(EntityResult.OPERATION_SUCCESSFUL);
		newResult.setCompressionThreshold(vRes[0].getCompressionThreshold());
		newResult.setType(EntityResult.DATA_RESULT);

		List<Object> columnList = EntityResultTools.getColumnList(doUnionAll);
		EntityResultTools.initEntityResult(newResult, columnList);

		int num = doUnionAll.calculateRecordNumber();
		for (int i = 0; i < num; i++) {
			Hashtable currentValues = doUnionAll.getRecordValues(i);
			if (!EntityResultTools.checkRecordExists(newResult, currentValues, columnList)) {
				newResult.addRecord(currentValues);
			}
		}

		return newResult;
	}

	private static boolean checkRecordExists(EntityResult newResult, Hashtable currentValues, List<Object> columnList) {
		EntityResult dofilter = EntityResultTools.dofilter(newResult, currentValues);

		// Moreover is required to check for this fields that sattisfy filter if is exactly the same value in all columns
		// because if currentvalues hasn`t some value in some column, will be not applied filter
		for (int i = 0; i < dofilter.calculateRecordNumber(); i++) {
			Hashtable filterValues = dofilter.getRecordValues(i);

			boolean match = true;
			for (Object col : columnList) {
				Object toLookForValue = currentValues.get(col);
				Object matchValue = filterValues.get(col);
				if (!ObjectTools.safeIsEquals(toLookForValue, matchValue)) {
					match = false;
					break;
				}
			}
			if (match) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Obtiene la lista de columnas de todos los {@link EntityResult}.
	 *
	 * @param vRes
	 *            the v res
	 * @return the column list
	 */
	private static List<Object> getColumnList(EntityResult... vRes) {
		List<Object> list = new ArrayList<Object>();
		for (EntityResult er : vRes) {
			Enumeration keys = er.keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				if (!list.contains(key)) {
					list.add(key);
				}
			}
		}
		return list;
	}

	/**
	 * Filtra un {@link EntityResult} a partir del filtro establecido en keysValues. Acepta SearchValue AUN NO ESTA MUY PROBADO!!!!!!
	 *
	 * @param er
	 *            the er
	 * @param keysValues
	 *            the keys values
	 * @return the entity result
	 */
	public static EntityResult dofilter(EntityResult er, Hashtable<?, ?> keysValues) {
		return EntityResultTools.dofilter(er, keysValues, false);
	}

	/**
	 * Filtra un {@link EntityResult} a partir del filtro establecido en keysValues. Acepta SearchValue AUN NO ESTA MUY PROBADO!!!!!!
	 *
	 * @param er
	 *            the er
	 * @param keysValues
	 *            the keys values
	 * @return the entity result
	 */
	public static EntityResult dofilter(EntityResult er, Hashtable<?, ?> keysValues, boolean remove) {
		EntityResult res = new EntityResult();
		res.setCode(EntityResult.OPERATION_SUCCESSFUL);
		res.setCompressionThreshold(er.getCompressionThreshold());
		res.setType(EntityResult.DATA_RESULT);
		// primero sacamos la lista de columnas totales
		List<Object> columnList = EntityResultTools.getColumnList(er);
		EntityResultTools.initEntityResult(res, columnList);
		int nregs = er.calculateRecordNumber();
		for (int i = 0; i < nregs; i++) {
			if (EntityResultTools.checkFilter(er, i, keysValues)) {
				res.addRecord(er.getRecordValues(i));
				if (remove) {
					er.deleteRecord(i);
					i--;
					nregs--;
				}
			}
		}

		return res;
	}

	/**
	 * Comprueba si una fila del {@link EntityResult} cumple el filtro establecido por keysValues.
	 *
	 * @param er
	 *            the er
	 * @param index
	 *            the index
	 * @param keysValues
	 *            the keys values
	 * @return true, if successful
	 */
	private static boolean checkFilter(EntityResult er, int index, Hashtable<?, ?> keysValues) {
		if (keysValues == null) {
			return true;
		}
		Enumeration<?> keys = keysValues.keys();
		boolean isOk = true;
		while (keys.hasMoreElements() && isOk) {
			Object filterKey = keys.nextElement();
			Object filterValue = keysValues.get(filterKey);
			Object test = ((Vector) er.get(filterKey)).get(index);

			if (filterValue instanceof SearchValue) {
				SearchValue sv = (SearchValue) filterValue;
				switch (sv.getCondition()) {
					case SearchValue.BETWEEN:
						if (test instanceof Comparable) {
							Comparable n = (Comparable) test;
							Comparable nMin = (Comparable) ((List) sv.getValue()).get(0);
							Comparable nMax = (Comparable) ((List) sv.getValue()).get(1);
							isOk = (n.compareTo(nMin) >= 0) && (n.compareTo(nMax) <= 0);
						} else {
							isOk = false;
						}
						break;
					case SearchValue.NOT_BETWEEN:
						if (test instanceof Comparable) {
							Comparable n = (Comparable) test;
							Comparable nMin = (Comparable) ((List) sv.getValue()).get(0);
							Comparable nMax = (Comparable) ((List) sv.getValue()).get(1);
							isOk = (n.compareTo(nMin) < 0) || (n.compareTo(nMax) > 0);
						} else {
							isOk = false;
						}
						break;
					case SearchValue.EQUAL:
						isOk = sv.getValue().equals(test);
						break;
					case SearchValue.NOT_EQUAL:
						isOk = !sv.getValue().equals(test);
						break;
					case SearchValue.IN:
					case SearchValue.OR:
						Vector v = (Vector) sv.getValue();
						isOk = v.contains(test);
						break;
					case SearchValue.NOT_IN:
						v = (Vector) sv.getValue();
						isOk = !v.contains(test);
						break;
					case SearchValue.LESS:
						if (test instanceof Comparable) {
							Comparable n = (Comparable) test;
							Comparable m = (Comparable) sv.getValue();
							isOk = n.compareTo(m) < 0;
						} else {
							isOk = false;
						}
						break;
					case SearchValue.LESS_EQUAL:
						if (test instanceof Comparable) {
							Comparable n = (Comparable) test;
							Comparable m = (Comparable) sv.getValue();
							isOk = n.compareTo(m) <= 0;
						} else {
							isOk = false;
						}
						break;
					case SearchValue.MORE:
						if (test instanceof Comparable) {
							Comparable n = (Comparable) test;
							Comparable m = (Comparable) sv.getValue();
							isOk = n.compareTo(m) > 0;
						} else {
							isOk = false;
						}
						break;
					case SearchValue.MORE_EQUAL:
						if (test instanceof Comparable) {
							Comparable n = (Comparable) test;
							Comparable m = (Comparable) sv.getValue();
							isOk = n.compareTo(m) >= 0;
						} else {
							isOk = false;
						}
						break;
					case SearchValue.NOT_NULL:
						isOk = test != null;
						break;
					case SearchValue.NULL:
						isOk = test == null;
						break;

					case SearchValue.EXISTS:
					default:
						break;
				}
			} else {
				isOk = filterValue.equals(test);
			}
		}
		return isOk;
	}

	/**
	 * Anade una columna al {@link EntityResult} con los valores indicados en columnDefaultValue.
	 *
	 * @param er
	 *            the er
	 * @param columnKey
	 *            the column key
	 * @param columnDefaultValue
	 *            the column default value
	 */
	public static void addColumn(EntityResult er, Object columnKey, Object columnDefaultValue) {
		int nreg = er.calculateRecordNumber();
		Object[] array = new Object[nreg];
		Arrays.fill(array, columnDefaultValue);
		er.put(columnKey, new Vector(Arrays.asList(array)));
	}

	/**
	 * Permite actualizar filas de un {@link EntityResult} que cumplen un criterio.
	 *
	 * @param er
	 *            the er
	 * @param updater
	 *            the updater
	 * @param criteria
	 *            the criteria
	 */
	public static void update(EntityResult er, IRowUpdater updater, Hashtable<?, ?> criteria) {
		if (er == null) {
			return;
		}

		int nres = er.calculateRecordNumber();
		for (int i = 0; i < nres; i++) {
			if (EntityResultTools.checkFilter(er, i, criteria)) {
				updater.updateRow(er, i);
			}
		}
	}

	/**
	 * Reemplaza en la columna el valueToReplace por el newValue.
	 *
	 * @param rs
	 *            the rs
	 * @param columnName
	 *            the column name
	 * @param valueToReplace
	 *            the value to replace
	 * @param newValue
	 *            the new value
	 */
	public static void replaceValue(EntityResult rs, String columnName, Object valueToReplace, Object newValue) {
		Vector testVector = (Vector) rs.get(columnName);
		if (testVector != null) {
			for (int i = 0; i < testVector.size(); i++) {
				Object test = testVector.get(i);
				if ((test == null) && (valueToReplace == null)) {
					testVector.set(i, newValue);
				} else if ((test != null) && test.equals(valueToReplace)) {
					testVector.set(i, newValue);
				}
			}
		}
	}

	/**
	 * Convierte a mayusculas los datos de la columna indicada.
	 *
	 * @param res
	 *            the res
	 * @param columnNames
	 *            the column names
	 */
	public static void doUpper(EntityResult res, String... columnNames) {
		if ((columnNames == null) || (res == null)) {
			return;
		}
		for (String columnName : columnNames) {
			List v = (List) res.get(columnName);
			if (v != null) {
				for (int i = 0; i < v.size(); i++) {
					Object ob = v.get(i);
					if (ob instanceof String) {
						v.set(i, ((String) ob).toUpperCase());
					}
				}
			}
		}
	}

	/**
	 * Compose a complex BasicExpresion based on IN exresion, consideing to separate large list in shorter blocks, to build a valid sentence.
	 *
	 * @param listValues
	 *            the list values
	 * @param field
	 *            the field
	 * @return the basic expression
	 */
	public static BasicExpression cutThousandsInExpresions(List<?> listValues, String field) {
		if (listValues == null) {
			return null;
		}

		Vector<Object> vThousands = new Vector<Object>();
		BasicExpression be = null;
		int counter = 0;
		for (Object item : listValues) {
			counter++;
			vThousands.add(item);
			if (counter == (EntityResultTools.MAX_IN_EXPRESSION_SUPPORT - 1)) {
				BasicExpression be1 = new BasicExpression(new BasicField(field), BasicOperator.IN_OP, vThousands);
				if (be == null) {
					be = be1;
				} else {
					be = new BasicExpression(be, BasicOperator.OR_OP, be1);
				}
				counter = 0;
				vThousands = new Vector<Object>();
			}
		}
		if (!vThousands.isEmpty()) {
			BasicExpression be1 = new BasicExpression(new BasicField(field), BasicOperator.IN_OP, vThousands);
			if (be == null) {
				be = be1;
			} else {
				be = new BasicExpression(be, BasicOperator.OR_OP, be1);
			}
		}
		return be;
	}

	/**
	 * Metodo auxiliar para crear rapidamente un registro en base a las claves y los valores.
	 *
	 * @param keys
	 *            the keys
	 * @param values
	 *            the values
	 * @return the hashtable
	 */
	public static Hashtable<String, Object> createRecord(String[] keys, Object[] values) {
		Hashtable<String, Object> record = new Hashtable<String, Object>();

		for (int i = 0; i < keys.length; i++) {
			if ((values[i] != null) && (keys[i] != null)) {
				record.put(keys[i], values[i]);
			}
		}

		return record;
	}

	/**
	 * Interfaz para poder realizar updates sobre un {@link EntityResult}.
	 */
	public interface IRowUpdater {

		/**
		 * Update row.
		 *
		 * @param er
		 *            the er
		 * @param row
		 *            the row
		 */
		void updateRow(EntityResult er, int row);
	}

	public static Vector<String> attributes(String... attributes) {
		if ((attributes == null) || (attributes.length == 0)) {
			return new Vector<String>();
		}
		return new Vector(Arrays.asList(attributes));
	}

	/**
	 * Utility to use with Ontimize entities, to specify filters(keys-values) more fast. <br> Will be received a sequence of objects with
	 * <key><value>[<key><value>...]. <br> Example: query(EntityResultTools.keysvalues("AA", new Integer(1), "BB", identifier), av, ses, con).
	 *
	 * @param objects
	 * @return
	 */
	public static Hashtable<Object, Object> keysvalues(Object... objects) {
		if (objects == null) {
			return new Hashtable<Object, Object>();
		}
		if ((objects.length % 2) != 0) {
			throw new RuntimeException("Review filters, it is mandatory to set dual <key><value>.");
		}
		for (Object o : objects) {
			if (o == null) {
				throw new RuntimeException("Review filters, it is not acceptable null <key> or null <value>.");
			}
		}

		Hashtable<Object, Object> res = new Hashtable<Object, Object>();
		int i = 0;
		while (i < objects.length) {
			res.put(objects[i++], objects[i++]);
		}
		return res;
	}

	/**
	 * Remove duplicates from an EntityResult.
	 *
	 * @param res
	 *            the res
	 * @return the entity result
	 */
	public static EntityResult doRemoveDuplicates(EntityResult in) {
		EntityResult res = new EntityResult();
		EntityResultTools.initEntityResult(res, new ArrayList(in.keySet()));
		int nInRegs = in.calculateRecordNumber();
		int nResRegs = 0;
		for (int i = 0; i < nInRegs; i++) {
			Map<?, ?> row = in.getRecordValues(i);
			if (!EntityResultTools.containsRow(res, nResRegs, row)) {
				res.addRecord((Hashtable) row);
				nResRegs++;
			}
		}
		return res;
	}

	/**
	 * Contains row.
	 *
	 * @param res
	 *            the res
	 * @param row
	 *            the row
	 * @return true, if successful
	 */
	public static boolean containsRow(EntityResult res, Map<?, ?> row) {
		if (res == null) {
			return false;
		}
		if (row == null) {
			return true;
		}
		return EntityResultTools.containsRow(res, res.calculateRecordNumber(), row);
	}

	/**
	 * Contains row.
	 *
	 * @param res
	 *            the res
	 * @param nregs
	 *            the nregs
	 * @param row
	 *            the row
	 * @return true, if successful
	 */
	public static boolean containsRow(EntityResult res, int nregs, Map<?, ?> row) {
		if (row == null) {
			return true;
		}
		if ((res == null) || (nregs == 0)) {
			return false;
		}
		for (int i = 0; i < nregs; i++) {
			Map<?, ?> record = res.getRecordValues(i);
			if (row.equals(record)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Iterates <code>attr</code> list and delete {@link TableAttribute} and {@link ReferenceFieldAttribute} if it's contained in
	 * <code>toDelete</code> list
	 *
	 * @param toDelete
	 *            names to delete
	 * @param attr
	 *            atributes
	 */
	public static void cleanNonRequiredTableAttributes(List<String> toDelete, List<String> attr) {
		for (int i = 0; i < attr.size(); i++) {
			Object ob = attr.get(i);
			String nameToDelete = null;
			if (ob instanceof TableAttribute) {
				nameToDelete = ((TableAttribute) ob).getEntity();
			} else if (ob instanceof String) {
				nameToDelete = (String) ob;
			} else if (ob instanceof ReferenceFieldAttribute) {
				nameToDelete = ((ReferenceFieldAttribute) ob).getEntity();
			}

			if ((nameToDelete != null) && toDelete.contains(nameToDelete)) {
				attr.remove(i);
				i--;
			}
		}
	}

	public static SearchValue betweenDatesExpression(Object leftValue, Object rightValue) {
		if ((leftValue != null) && (rightValue != null)) {
			return new SearchValue(SearchValue.BETWEEN, new Vector(Arrays.asList(leftValue, rightValue)));
		} else {
			if (leftValue != null) {
				return new SearchValue(SearchValue.MORE_EQUAL, leftValue);
			} else if (rightValue != null) {
				return new SearchValue(SearchValue.LESS_EQUAL, rightValue);
			} else {
				return null;
			}
		}
	}

	/**
	 * Compare both ERs, that can have different order.
	 *
	 * @param erBackup
	 * @param er
	 * @return true if exact match, false in other case
	 */
	public static boolean compare(EntityResult erBackup, EntityResult er) {
		if (((erBackup == null) || erBackup.isEmpty()) && ((er != null) && !er.isEmpty())) {
			return false;
		} else if (((er == null) || er.isEmpty()) && ((erBackup != null) && !erBackup.isEmpty())) {
			return false;
		} else if (er.calculateRecordNumber() != erBackup.calculateRecordNumber()) {
			return false;
		} else {

			int num = erBackup.calculateRecordNumber();
			for (int i = 0; i < num; i++) {
				if (!EntityResultTools.containsRow(er, erBackup.getRecordValues(i))) {
					return false;
				}
			}
			return true;
		}

	}
}
