package com.ontimize.jee.server.dao.jdbc;

import java.util.List;

import com.ontimize.jee.server.dao.jdbc.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.jdbc.setup.FunctionColumnType;

/**
 * Clase que almacena la información de una plantilla de consulta SQL.
 *
 */
public class QueryTemplateInformation {

	/** The sql template. */
	private String						sqlTemplate;

	/** The ambiguous columns. */
	private List<AmbiguousColumnType>	ambiguousColumns;

	/** The function columns. */
	private List<FunctionColumnType>	functionColumns;

	/** The valid columns. */
	private List<String>				validColumns;

	/**
	 * Instantiates a new query template information.
	 *
	 * @param sql
	 *            the sql
	 * @param ambiguousColumns
	 *            the ambiguous columns
	 * @param functionColumns
	 *            the function columns
	 */
	public QueryTemplateInformation(String sql, List<AmbiguousColumnType> ambiguousColumns, List<FunctionColumnType> functionColumns,
			List<String> validColumns) {
		this();
		this.sqlTemplate = sql;
		this.ambiguousColumns = ambiguousColumns;
		this.functionColumns = functionColumns;
		this.validColumns = validColumns;
	}

	/**
	 * Instantiates a new query template information.
	 */
	public QueryTemplateInformation() {
		super();
	}

	/**
	 * Gets the sql template.
	 *
	 * @return the sqlTemplate
	 */
	public String getSqlTemplate() {
		return this.sqlTemplate;
	}

	/**
	 * Sets the sql template.
	 *
	 * @param sqlTemplate
	 *            the sqlTemplate to set
	 */
	public void setSqlTemplate(String sqlTemplate) {
		this.sqlTemplate = sqlTemplate;
	}

	/**
	 * Gets the ambiguous columns.
	 *
	 * @return the ambiguousColumns
	 */
	public List<AmbiguousColumnType> getAmbiguousColumns() {
		return this.ambiguousColumns;
	}

	/**
	 * Sets the ambiguous columns.
	 *
	 * @param ambiguousColumns
	 *            the ambiguousColumns to set
	 */
	public void setAmbiguousColumns(List<AmbiguousColumnType> ambiguousColumns) {
		this.ambiguousColumns = ambiguousColumns;
	}

	/**
	 * Gets the function columns.
	 *
	 * @return the function columns
	 */
	public List<FunctionColumnType> getFunctionColumns() {
		return this.functionColumns;
	}

	/**
	 * Sets the function columns.
	 *
	 * @param functionColumns
	 *            the new function columns
	 */
	public void setFunctionColumns(List<FunctionColumnType> functionColumns) {
		this.functionColumns = functionColumns;
	}

	/**
	 * Gets the valid columns.
	 *
	 * @return
	 */
	public List<String> getValidColumns() {
		return this.validColumns;
	}

	/**
	 * Sets the valid columns.
	 *
	 * @param validColumns
	 */
	public void setValidColumns(List<String> validColumns) {
		this.validColumns = validColumns;
	}
}