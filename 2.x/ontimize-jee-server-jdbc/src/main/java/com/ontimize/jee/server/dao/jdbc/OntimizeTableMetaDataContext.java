package com.ontimize.jee.server.dao.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.metadata.TableMetaDataContext;
import org.springframework.jdbc.core.metadata.TableMetaDataProvider;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.server.dao.common.INameConvention;

/**
 * The Class OntimizeTableMetaDataContext.
 */
public class OntimizeTableMetaDataContext extends TableMetaDataContext {

	private TableMetaDataProvider	metaDataProviderCopy;
	private boolean					processed;
	private INameConvention			nameConvention;

	/**
	 * Instantiates a new ontimize table meta data context.
	 */
	public OntimizeTableMetaDataContext() {
		super();
		this.processed = false;
		this.metaDataProviderCopy = null;
	}

	/**
	 * Gets the meta data provider.
	 *
	 * @return the meta data provider
	 */
	protected TableMetaDataProvider getMetaDataProvider() {
		if (this.metaDataProviderCopy == null) {
			this.metaDataProviderCopy = (TableMetaDataProvider) ReflectionTools.getFieldValue(this, "metaDataProvider");
		}
		return this.metaDataProviderCopy;
	}

	public INameConvention getNameConvention() {
		return this.nameConvention;
	}

	public void setNameConvention(INameConvention nameConvention) {
		this.nameConvention = nameConvention;
	}

	/**
	 * Gets the table parameters.
	 *
	 * @return the table parameters
	 */
	public List<TableParameterMetaData> getTableParameters() {
		return this.getMetaDataProvider().getTableParameterMetaData();
	}

	/**
	 * Match the provided column names and values with the list of columns used.
	 *
	 * @param inParameters
	 *            the parameter names and values
	 */
	public InsertMetaInfoHolder getInsertMetaInfo(Map<String, ?> inParameters) {
		Map<String, Object> inParametersCase = new HashMap<>(inParameters.size());
		for (Entry<String, ?> entry : inParameters.entrySet()) {
			inParametersCase.put(this.nameConvention.convertName(entry.getKey()), entry.getValue());
		}
		List<Object> values = new ArrayList<>();
		List<Integer> sqlTypes = new ArrayList<>();
		List<String> validColumns = new ArrayList<>();

		for (Entry<String, ?> entry : inParametersCase.entrySet()) {
			for (TableParameterMetaData tableColumn : this.getMetaDataProvider().getTableParameterMetaData()) {
				if (this.nameConvention.convertName(tableColumn.getParameterName()).equals(entry.getKey())) {
					values.add(entry.getValue());
					sqlTypes.add(tableColumn.getSqlType());
					validColumns.add(tableColumn.getParameterName());
					break;
				}
			}
		}

		if (validColumns.isEmpty()) {
			throw new InvalidDataAccessApiUsageException("No columns o insert in table '" + this.getTableName() + "' so an insert statement can't be generated");
		}

		String insertString = this.createInsertStringForColumns(validColumns);

		return new InsertMetaInfoHolder(values, insertString, ArrayUtils.toPrimitive(sqlTypes.toArray(new Integer[] {})));
	}

	/**
	 * Match the provided column names and values with the list of columns used.
	 *
	 * @param parameterSource
	 *            the parameter names and values
	 */
	public InsertMetaInfoHolder getInsertMetaInfo(SqlParameterSource parameterSource) {
		// for parameter source lookups we need to provide caseinsensitive lookup support since the
		// database metadata is not necessarily providing case sensitive column names
		Map<String, String> caseInsensitiveParameterNames = SqlParameterSourceUtils.extractCaseInsensitiveParameterNames(parameterSource);

		List<Object> values = new ArrayList<>();
		List<Integer> sqlTypes = new ArrayList<>();
		List<String> validColumns = new ArrayList<>();

		for (TableParameterMetaData tableColumn : this.getMetaDataProvider().getTableParameterMetaData()) {
			if (caseInsensitiveParameterNames.containsKey(tableColumn.getParameterName().toLowerCase())) {
				values.add(SqlParameterSourceUtils.getTypedValue(parameterSource, caseInsensitiveParameterNames.get(tableColumn.getParameterName().toLowerCase())));
				sqlTypes.add(tableColumn.getSqlType());
				validColumns.add(tableColumn.getParameterName());
			}
		}

		if (validColumns.isEmpty()) {
			throw new InvalidDataAccessApiUsageException("No columns o insert in table '" + this.getTableName() + "' so an insert statement can't be generated");
		}

		String insertString = this.createInsertStringForColumns(validColumns);

		return new InsertMetaInfoHolder(values, insertString, ArrayUtils.toPrimitive(sqlTypes.toArray(new Integer[] {})));
	}

	/**
	 * Build the insert string based on configuration and metadata information
	 *
	 * @return the insert string to be used
	 */
	protected String createInsertStringForColumns(List<String> columns) {
		StringBuilder insertStatement = new StringBuilder();
		insertStatement.append("INSERT INTO ");
		if (this.getSchemaName() != null) {
			insertStatement.append(this.getSchemaName());
			insertStatement.append(".");
		}
		insertStatement.append(this.getTableName());
		insertStatement.append(" (");
		int columnCount = 0;
		for (String columnName : columns) {
			columnCount++;
			if (columnCount > 1) {
				insertStatement.append(", ");
			}
			insertStatement.append(columnName);
		}
		insertStatement.append(") VALUES(");
		for (int i = 0; i < columnCount; i++) {
			if (i > 0) {
				insertStatement.append(", ");
			}
			insertStatement.append("?");
		}
		insertStatement.append(")");
		return insertStatement.toString();
	}

	/**
	 * Get a List of the table upper case column names.
	 *
	 * @deprecated
	 */
	@Deprecated
	public List<String> getUpperCaseTableColumns() {
		List<String> tableColumns = this.getTableColumns();
		return this.changeColumnNameToUpperCase(tableColumns);
	}

	public List<String> getNameConventionTableColumns() {
		List<String> tableColumns = this.getTableColumns();
		return this.changeColumnNameToNameConvention(tableColumns);
	}

	/**
	 * Get a List of the table upper case column names.
	 *
	 * @deprecated
	 */
	@Deprecated
	private List<String> changeColumnNameToUpperCase(List<String> columnNames) {
		for (Object columnName : columnNames) {
			if (columnName instanceof String) {
				columnNames.set(columnNames.indexOf(columnName), ((String) columnName).toUpperCase());
			}
		}
		return columnNames;
	}

	private List<String> changeColumnNameToNameConvention(List<String> columnNames) {
		for (Object columnName : columnNames) {
			if (columnName instanceof String) {
				columnNames.set(columnNames.indexOf(columnName), this.nameConvention.convertName((String) columnName));
			}
		}
		return columnNames;
	}

	@Override
	public void processMetaData(DataSource dataSource, List<String> declaredColumns, String[] generatedKeyNames) {
		super.processMetaData(dataSource, declaredColumns, generatedKeyNames);
		this.processed = true;
	}

	public boolean isProcessed() {
		return this.processed;
	}

}
