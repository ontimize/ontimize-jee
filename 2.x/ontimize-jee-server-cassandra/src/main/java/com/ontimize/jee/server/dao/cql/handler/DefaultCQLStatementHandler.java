package com.ontimize.jee.server.dao.cql.handler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.ontimize.db.EntityResult;
import com.ontimize.db.SQLStatementBuilder;
import com.ontimize.db.SQLStatementBuilder.SQLConditionValuesProcessor;
import com.ontimize.db.SQLStatementBuilder.SQLNameEval;
import com.ontimize.db.SQLStatementBuilder.SQLOrder;
import com.ontimize.db.util.DBFunctionName;
import com.ontimize.jee.server.dao.cql.CQLStatement;

public class DefaultCQLStatementHandler implements CQLStatementHandler {

	protected SQLConditionValuesProcessor	queryConditionsProcessor	= new SQLStatementBuilder.ExtendedSQLConditionValuesProcessor(false, true);

	private static final Logger logger = LoggerFactory.getLogger(DefaultCQLStatementHandler.class);

	protected SQLNameEval		sqlNameEval;

	protected static final String			ALLOW_FILTERING				= " ALLOW FILTERING ";

	protected char[]			CONFLICT_CHARS	= { ' ', '/', '-', 'Á', 'É', 'Í', 'Ó', 'Ú', 'á', 'é', 'í', 'ó', 'ú', '%' };
	/**
	 * Transforms a com.datastax.driver.core.ResultSet object into an Ontimize {@link EntityResult}. The columns in the ResultSet are the keys in the EntityResult, and the values
	 * for the columns are stored in Vector objects corresponding to the keys in the EntityResult.
	 * <p>
	 * The following getxxxxx ResulSet methods are used for getting column data:
	 * <ul>
	 * <li>getBlob for BLOB SQLType</li>
	 * <li>getClob for CLOB SQLType</li>
	 * <li>getBinaryStream for VARBINARY or LOGVARBINARY SQLType</li>
	 * <li>getAsciiStream for LONGVARCHAR SQLType</li>
	 * <li>getObject for rest of SQLTypes</li>
	 * </ul>
	 *
	 * @param resultSet
	 *            the source ResultSet
	 * @param entityResult
	 *            the destination EntityResult. It has a Hashtable of Vectors structure.
	 * @param recordNumber
	 *            Number of records to query
	 * @param offset
	 *            number of the row where start
	 * @param delimited
	 *            If delimited is true then all the resultSet must be queried into the EntityResult, because the original query that generated the ResultSet only returns the number
	 *            of records specified in <code>recordNumber</code>. If delimited is false then this method only read the specified <code>recordNumber</code> from the ResultSet
	 *            into the EntityResult
	 * @param columnNames
	 *            Names of the columns to return in the EntityResult. If this parameter is null or empty then return exactly the same names in the ResultSet
	 * @throws Exception
	 *             if any error (database, etc.) occurs
	 */
	@Override
	public void resultSetToEntityResult(ResultSet resultSet, EntityResult entityResult, List<String> columnNames) throws Exception {
		try {
			ColumnDefinitions columnDefinitions = resultSet.getColumnDefinitions();
			// Optimization: Array access, instead of request the name in each
			// loop

			List<Definition> definitions = columnDefinitions.asList();

			//			String[] sColumnNames = this.getColumnNames(rsMetaData);

			Hashtable hColumnTypesAux = new Hashtable();
			for (Definition definition : definitions) {
				DataType dataType = definition.getType();
				hColumnTypesAux.put(definition.getName(), definition.getType().getName());
			}
			entityResult.setColumnSQLTypes(hColumnTypesAux);
			int rowCount = resultSet.getAvailableWithoutFetching();
			Iterator<Row> iterator = resultSet.iterator();

			while (iterator.hasNext()) {
				Row rowData = iterator.next();
				for (Definition definition : definitions) {
					String columnName = definition.getName();
					Object oValue = this.getResultSetValue(rowData, definition);
					Vector vPreviousData = (Vector) entityResult.get(columnName);
					if (vPreviousData != null) {
						vPreviousData.add(oValue);
					} else {
						Vector vData = new Vector(rowCount);
						vData.add(oValue);
						entityResult.put(columnName, vData);
					}
				}
			}
			this.changeColumnNames(entityResult, columnNames);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected Object getResultSetValue(Row rowData, Definition definition) throws Exception {
		if (DataType.blob().equals(definition.getType())) {
			ByteBuffer buffer = rowData.getBytes(definition.getName());
			byte[] b = new byte[buffer.remaining()];
			buffer.get(b);
			return buffer;
		}else{
			return rowData.getObject(definition.getName());
		}
	}

	protected void changeColumnNames(EntityResult result, List columnNames) {
		if (columnNames != null) {
			for (int i = 0; i < columnNames.size(); i++) {
				Object columnName = columnNames.get(i);
				if (columnName != null) {
					if (!result.containsKey(columnName)) {
						// Search the same columnName but to uppercase or to
						// lowercase
						if (result.containsKey(columnName.toString().toUpperCase())) {
							this.changeColumnName(result, columnName.toString().toUpperCase(), columnName.toString());
						} else if (result.containsKey(columnName.toString().toLowerCase())) {
							this.changeColumnName(result, columnName.toString().toLowerCase(), columnName.toString());
						}
					}
				}
			}
		}
	}

	protected void changeColumnName(EntityResult result, String nameColumn, String replaceByColumn) {
		if (result.containsKey(nameColumn)) {
			result.put(replaceByColumn, result.remove(nameColumn));
			Hashtable sqlTypes = result.getColumnSQLTypes();
			if ((sqlTypes != null) && sqlTypes.containsKey(nameColumn)) {
				sqlTypes.put(replaceByColumn, sqlTypes.get(nameColumn));
			}

			List order = result.getOrderColumns();
			if ((order != null) && order.contains(nameColumn)) {
				int index = order.indexOf(nameColumn);
				order.remove(index);
				order.add(index, replaceByColumn);
				result.setColumnOrder(order);
			}
		}
	}

	@Override
	public CQLStatement createSelectQuery(String table, List<?> requestedColumns, Map<?, ?> conditions, List<String> wildcards, List<SQLOrder> columnSorting) {
		return this.createSelectQuery(table, requestedColumns, conditions, wildcards, columnSorting, false);
	}

	public CQLStatement createSelectQuery(String table, List<?> requestedColumns, Map<?, ?> conditions, List<String> wildcards, List<SQLOrder> columnSorting,
			boolean forceDistinct) {
		StringBuffer sql = new StringBuffer();
		List<String> vValues = new ArrayList<>();

		if ((columnSorting != null) && (requestedColumns.isEmpty() == false) && forceDistinct) {
			for (int i = 0; i < columnSorting.size(); i++) {
				if (requestedColumns.contains(columnSorting.get(i).toString()) == false) {
					((List) requestedColumns).add(columnSorting.get(i).toString());
				}
			}
		}

		// TODO REVIEW
		DBFunctionName function = this.hasIsolationFunction(requestedColumns);
		if (function == null) {
			sql.append(this.createSelectQuery(table, (List) requestedColumns, forceDistinct));
		} else {
			Vector temp = new Vector();
			temp.add(function);
			sql.append(this.createSelectQuery("", temp, false));
			sql.append("(");
			sql.append(this.createSelectQuery(table, (List) requestedColumns, forceDistinct));
		}

		String cond = this.createQueryConditions(conditions, wildcards, vValues);
		if (cond != null) {
			sql.append(cond);
		}
		if ((columnSorting != null) && (columnSorting.isEmpty() == false)) {
			String sort = this.createSortStatement(columnSorting);
			sql.append(sort);
		}

		if (function != null) {
			sql.append(")");
		}

		sql.append(DefaultCQLStatementHandler.ALLOW_FILTERING);
		DefaultCQLStatementHandler.logger.debug(sql.toString());
		return new CQLStatement(sql.toString(), vValues);
	}

	protected String createQueryConditions(Map<?, ?> conditions, List<String> wildcards, List<String> values) {
		StringBuffer sbStringQuery = new StringBuffer(" ");
		// Create the conditions string.
		if ((conditions == null) || conditions.isEmpty()) {
			// If there are no conditions query is finished
			return null;
		} else {
			sbStringQuery.append(SQLStatementBuilder.WHERE);
		}

		Vector vValues = new Vector();
		sbStringQuery.append(this.queryConditionsProcessor.createQueryConditions(new Hashtable<>(conditions), new Vector<>(wildcards), vValues));
		values.addAll(vValues);

		return sbStringQuery.toString();
	}

	protected DBFunctionName hasIsolationFunction(List<?> requestedColumns) {
		if (requestedColumns == null) {
			return null;
		}
		for (int i = 0; i < requestedColumns.size(); i++) {
			if ((requestedColumns.get(i) instanceof DBFunctionName) && ((DBFunctionName) requestedColumns.get(i)).isIsolated()) {
				DBFunctionName function = (DBFunctionName) requestedColumns.remove(i);
				return function;
			}
		}
		return null;
	}

	public void setSQLNameEval(SQLNameEval eval) {
		this.sqlNameEval = eval;
	}

	public SQLNameEval getSQLNameEval() {
		return this.sqlNameEval;
	}

	public boolean checkColumnName(String columnName) {
		if (this.sqlNameEval != null) {
			return this.sqlNameEval.needCorch(columnName);
		}
		boolean bBrackets = false;
		if (columnName.toUpperCase().indexOf(" AS ") >= 0) {
			String columnNameNoAs = columnName.toUpperCase().replaceAll(" AS ", "");
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

	protected String createSelectClause(boolean forceDistinct) {
		StringBuffer selectClause = new StringBuffer(SQLStatementBuilder.SELECT);
		if (forceDistinct) {
			selectClause.append(SQLStatementBuilder.DISTINCT);
		}
		return selectClause.toString();
	}

	protected String createSelectQuery(String table, List<String> requestedColumns, boolean forceDistinct) {
		StringBuffer sStringQuery = new StringBuffer(this.createSelectClause(forceDistinct));
		String tableaux = table.toLowerCase();
		if ((requestedColumns == null) || (requestedColumns.isEmpty() == true)) {
			sStringQuery.append(SQLStatementBuilder.ASTERISK);
			sStringQuery.append(SQLStatementBuilder.FROM);
			if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table)) {
				sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
				sStringQuery.append(table);
				sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
			} else {
				sStringQuery.append(table);
			}
		} else {
			// If attributes is empty, then create the query with all requested
			// columns.
			// Requested columns number
			int attributesNumber = requestedColumns.size();
			for (int i = 0; i < attributesNumber; i++) {
				String sColumn = requestedColumns.get(i);
				if (sColumn == null) {
					continue;
				}

				// If some conflicted character is contained then use brackets
				boolean bBrackets = this.checkColumnName(sColumn);
				if (i < (attributesNumber - 1)) {
					if (bBrackets) {
						// since 5.2071EN-0.2
						String[] tables = table.split(",");
						String tablek = table;
						for (int k = 0; k < tables.length; k++) {
							if (sColumn.indexOf(tables[k] + ".") == 0) {
								tablek = tables[k];
								continue;
							}
						}
						if (!tablek.equals(table)) {
							StringBuffer sbsColumn = new StringBuffer(sColumn);
							// column begins with table name so brackets must be
							// placed where name of column begins not including
							// all (e.g.) table_name.[column]
							int indexOpenBracket = (tablek + ".").length();
							int indexCloseBracket = sColumn.length();
							int indexOpenBracketInAs = -1;
							int indexCloseBracketInAs = -1;
							if (sColumn.toUpperCase().indexOf(" AS ") >= 0) {
								indexCloseBracket = sColumn.toUpperCase().indexOf(" AS ");
								indexOpenBracketInAs = indexCloseBracket + 5;
								indexCloseBracketInAs = sColumn.length();
							}
							// need brackets without blank spaces, cannot use
							// SQLStatementBuilder.OPEN_SQUARE_BRACKET nor
							// SQLStatementBuilder.CLOSE_SQUARE_BRACKET
							if (indexCloseBracketInAs != -1) {
								sbsColumn.insert(indexCloseBracketInAs, "]");
							}
							sbsColumn.insert(indexCloseBracket, "]");
							if (indexOpenBracketInAs != -1) {
								sbsColumn.insert(indexOpenBracketInAs, "[");
							}
							sbsColumn.insert(indexOpenBracket, "[");
							sStringQuery.append(sbsColumn.toString());
							sStringQuery.append(SQLStatementBuilder.COMMA);
						}
						//
						else {
							sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
							sStringQuery.append(sColumn);
							sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
							sStringQuery.append(SQLStatementBuilder.COMMA);
						}
						// }

					} else {
						sStringQuery.append(sColumn);
						sStringQuery.append(SQLStatementBuilder.COMMA);
					}
				} else {
					if (bBrackets) {
						// since 5.2076EN-0.2
						String[] tables = table.split(",");
						String tablek = table;
						for (int k = 0; k < tables.length; k++) {
							if (sColumn.indexOf(tables[k] + ".") == 0) {
								tablek = tables[k];
								continue;
							}
						}
						if (!tablek.equals(table)) {
							StringBuffer sbsColumn = new StringBuffer(sColumn);
							// column begins with table name so brackets must be
							// placed where name of column begins not including
							// all (e.g.) table_name.[column]
							int indexOpenBracket = (tablek + ".").length();
							int indexCloseBracket = sColumn.length();
							int indexOpenBracketInAs = -1;
							int indexCloseBracketInAs = -1;
							if (sColumn.toUpperCase().indexOf(" AS ") >= 0) {
								indexCloseBracket = sColumn.toUpperCase().indexOf(" AS ");
								indexOpenBracketInAs = indexCloseBracket + 5;
								indexCloseBracketInAs = sColumn.length();
							}
							// need brackets without blank spaces, cannot use
							// SQLStatementBuilder.OPEN_SQUARE_BRACKET nor
							// SQLStatementBuilder.CLOSE_SQUARE_BRACKET
							if (indexCloseBracketInAs != -1) {
								sbsColumn.insert(indexCloseBracketInAs, "]");
							}
							sbsColumn.insert(indexCloseBracket, "]");
							if (indexOpenBracketInAs != -1) {
								sbsColumn.insert(indexOpenBracketInAs, "[");
							}
							sbsColumn.insert(indexOpenBracket, "[");
							sStringQuery.append(sbsColumn.toString());
						}
						//
						else {
							sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
							sStringQuery.append(sColumn);
							sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
						}
						sStringQuery.append(SQLStatementBuilder.FROM);
						if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table)) {
							sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
							sStringQuery.append(table);
							sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
						} else {
							sStringQuery.append(table);
						}
					} else {
						// The last attribute does not include the COMMA
						sStringQuery.append(sColumn);
						sStringQuery.append(SQLStatementBuilder.FROM);
						if ((tableaux.indexOf("select") < 0) && this.checkColumnName(table) && (tableaux.indexOf("join") < 0)) {
							sStringQuery.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
							sStringQuery.append(table);
							sStringQuery.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
						} else {
							sStringQuery.append(table);
						}
					}
				}
			}
		}
		return sStringQuery.toString();
	}

	protected String createSortStatement(List<SQLOrder> sortColumns) {
		if ((sortColumns == null) || sortColumns.isEmpty()) {
			return "";
		}

		StringBuffer sb = new StringBuffer(SQLStatementBuilder.ORDER_BY);
		boolean first = true;
		for (SQLOrder order : sortColumns) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			if (this.checkColumnName(order.getColumnName())) {
				sb.append(SQLStatementBuilder.OPEN_SQUARE_BRACKET);
				sb.append(order.getColumnName());
				sb.append(SQLStatementBuilder.CLOSE_SQUARE_BRACKET);
			}

			if (!order.isAscendent()) {
				sb.append(SQLStatementBuilder.DESC);
			}
		}
		return sb.toString();
	}
}
