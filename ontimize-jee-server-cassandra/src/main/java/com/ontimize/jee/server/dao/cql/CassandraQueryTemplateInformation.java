package com.ontimize.jee.server.dao.cql;

import java.util.List;

import com.ontimize.jee.server.dao.cql.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.cql.setup.FunctionColumnType;

/**
 * Clase que almacena la información de una plantilla de consulta CQL.
 *
 */
public class CassandraQueryTemplateInformation {

	/** The sql template. */
	private String						cqlTemplate;

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
	public CassandraQueryTemplateInformation(String cql, List<AmbiguousColumnType> ambiguousColumns, List<FunctionColumnType> functionColumns, List<String> validColumns) {
		this();
		this.cqlTemplate = cql;
		this.ambiguousColumns = ambiguousColumns;
		this.functionColumns = functionColumns;
		this.validColumns = validColumns;
	}

	/**
	 * Instantiates a new query template information.
	 */
	public CassandraQueryTemplateInformation() {
		super();
	}

	/**
	 * Gets the sql template.
	 *
	 * @return the sqlTemplate
	 */
	public String getCqlTemplate() {
		return this.cqlTemplate;
	}

	/**
	 * Sets the sql template.
	 *
	 * @param sqlTemplate
	 *            the sqlTemplate to set
	 */
	public void setCqlTemplate(String cqlTemplate) {
		this.cqlTemplate = cqlTemplate;
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