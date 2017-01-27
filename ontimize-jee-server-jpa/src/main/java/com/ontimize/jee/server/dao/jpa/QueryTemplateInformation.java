/**
 *
 */
package com.ontimize.jee.server.dao.jpa;

import java.util.List;

import com.ontimize.jee.server.dao.jpa.common.MappingInfo;
import com.ontimize.jee.server.dao.jpa.setup.AmbiguousColumnType;
import com.ontimize.jee.server.dao.jpa.setup.FunctionColumnType;

/**
 * Clase que almacena la informacion de una plantilla de consulta SQL.
 *
 */
public class QueryTemplateInformation {

    /**
     * The Enum Syntax.
     */
    public enum Syntax {

        /** The sql. */
        SQL,
        /** The hql. */
        JPQL
    }

    /** The sql template. */
    private String						sqlTemplate;

    /** The syntax. */
    private Syntax syntax;

    /** The result class. */
    private Class<?> resultClass;

    /** The mapping info. */
    private MappingInfo mappingInfo;

    /** The ambiguous columns. */
    private List<AmbiguousColumnType> ambiguousColumns;

    /** The function columns. */
    private List<FunctionColumnType> functionColumns;

    /**
     * Instantiates a new query template information.
     *
     * @param sql
     *            the sql
     * @param syntax
     *            the syntax
     * @param resultClass
     *            the result class
     * @param mappingInfo
     *            the mapping info
     * @param ambiguousColumns
     *            the ambiguous columns
     * @param functionColumns
     *            the function columns
     */
    public QueryTemplateInformation(final String sql, final Syntax syntax, final Class<?> resultClass, final MappingInfo mappingInfo, final List<AmbiguousColumnType> ambiguousColumns, final List<FunctionColumnType> functionColumns) {
        this();
        this.sqlTemplate = sql;
        this.syntax = syntax;
        this.resultClass = resultClass;
        this.mappingInfo = mappingInfo;
        this.ambiguousColumns = ambiguousColumns;
        this.functionColumns = functionColumns;
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
    public void setSqlTemplate(final String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    /**
     * Gets the syntax.
     *
     * @return the syntax
     */
    public Syntax getSyntax() {
        return this.syntax;
    }

    /**
     * Sets the syntax.
     *
     * @param syntax
     *            the new syntax
     */
    public void setSyntax(final Syntax syntax) {
        this.syntax = syntax;
    }

    /**
     * Gets the result class.
     *
     * @return the result class
     */
    public Class<?> getResultClass() {
        return this.resultClass;
    }

    /**
     * Sets the result class.
     *
     * @param resultClass
     *            the new result class
     */
    public void setResultClass(final Class<?> resultClass) {
        this.resultClass = resultClass;
    }

    /**
     * Gets the mapping info.
     *
     * @return the mapping info
     */
    public MappingInfo getMappingInfo() {
        return this.mappingInfo;
    }

    /**
     * Sets the mapping info.
     *
     * @param mappingInfo
     *            the new mapping info
     */
    public void setMappingInfo(final MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    /**
     * Gets the ambiguous columns.
     *
     * @return the ambiguous columns
     */
    public List<AmbiguousColumnType> getAmbiguousColumns() {
        return this.ambiguousColumns;
    }

    /**
     * Sets the ambiguous columns.
     *
     * @param ambiguousColumns
     *            the new ambiguous columns
     */
    public void setAmbiguousColumns(final List<AmbiguousColumnType> ambiguousColumns) {
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
    public void setFunctionColumns(final List<FunctionColumnType> functionColumns) {
        this.functionColumns = functionColumns;
    }
}
