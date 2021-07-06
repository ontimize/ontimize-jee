package com.ontimize.jee.common.db.sql;

import java.io.Serializable;

/**
 * Class that contains all the information necessary to define a column into a database.
 *
 * @author Imatia Innovation
 *
 */
public class SQLColumn implements Serializable, Cloneable {

    /**
     * The name of the column.
     */
    protected String columnName;

    /**
     * The SQl Type of the column.
     */
    protected int columnSQLType;

    /**
     * The SQL size of the column.
     */
    protected int columnSQLSize = -1;

    /**
     * Parameter that indicates if the column is UNIQUE.
     */
    protected boolean unique = false;

    /**
     * Parameter that indicates if the column is PRIMARY KEY.
     */
    protected boolean primaryKey = false;

    /**
     * Parameter that indicates if the column is required, that is, if it accepts or not Null values.
     */
    protected boolean required = false;

    /**
     * The default value to this column.
     */
    protected Object defaultValue = null;

    /**
     * Creates a new <code>SQLColumn</code> indicating the name of the column and the SQL Type of it.
     * @param columnName The name of the column.
     * @param columnSQLType The int value to the SQL Type.
     */
    public SQLColumn(String columnName, int columnSQLType) {
        this.columnName = columnName;
        this.columnSQLType = columnSQLType;
    }

    /**
     * Creates a new <code>SQLColumn</code> indicating the name of the column, the SQL Type of it and
     * the parameter primaryKey. If this parameter is true this column will be set as Primary Key.
     * @param columnName The name of the column.
     * @param columnSQLType The int value to the SQL Type.
     * @param primaryKey The value of parameter primaryKey. If it is true the column will be set as
     *        Primary Key.
     */
    public SQLColumn(String columnName, int columnSQLType, boolean primaryKey) {
        this(columnName, columnSQLType, primaryKey, false);
    }

    /**
     * Creates a new <code>SQLColumn</code> indicating the name of the column, the SQL Type of it, the
     * parameter primaryKey and the parameter required. If the parameter prikaryKey is true this column
     * will be set as Primary Key. As well if the parameter required is true, the column will be set as
     * Required, that is, it doesn't admit null values.
     * @param columnName The name of the column.
     * @param columnSQLType The int value to the SQL Type.
     * @param primaryKey The value of parameter primaryKey. If it is true the column will be set as
     *        Primary Key.
     * @param required The value of parameter required. If it is true the column will be set as Required
     *        (doesn't admit null values).
     */
    public SQLColumn(String columnName, int columnSQLType, boolean primaryKey, boolean required) {
        this(columnName, columnSQLType);
        this.primaryKey = primaryKey;
        this.required = required;
    }

    /**
     * Returns the name of the column.
     * @return a <code>String</code> with the column's name.
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Sets the name of the column.
     * @param columnName String with the name of the column.
     */
    public void setColumnName(String columnName) {
        if ((columnName == null) || (columnName.length() == 0)) {
            throw new IllegalArgumentException("The column name is a required value");
        }
        this.columnName = columnName;
    }

    /**
     * Returns the SQL Type of the column.
     * @return a <code>int</code> with the column's SQL Type.
     */
    public int getColumnSQLType() {
        return this.columnSQLType;
    }

    /**
     * Sets the SQL Type of the column.
     * @param columnSQLType An int with the SQL Type to this column.
     */
    public void setColumnSQLType(int columnSQLType) {
        this.columnSQLType = columnSQLType;
    }

    /**
     * Checks if the current column is part of a Primary Key. If it is true, the column is part of a
     * Primary Key
     * @return a <code>boolean</code>
     */
    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * Sets the value of the parameter primaryKey. If it is true it indicates that the column will be
     * Primary Key.
     * @param primaryKey A boolean that sets the value of the parameter primaryKey.
     */
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Checks if the current column is required. If it is true, the column doesn't admit null values.
     * @return a <code>boolean</code>
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Sets the value of the parameter required. If it is true it indicates that the column will be
     * required, that it, it doesn't admit null values.
     * @param required A boolean that sets the value of the parameter required.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns the default value to this column.
     * @return an <code>Object</code> with the default value.
     */
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Sets de default value to this column.
     * @param defaultValue Object with the default value.
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the SQL size of the column.
     * @return a <code>int</code> with the SQL size of the column.
     */
    public int getColumnSQLSize() {
        return this.columnSQLSize;
    }

    /**
     * Sets the SQL size of the column.
     * @param columnSQLSize int with the SQL size of the column.
     */
    public void setColumnSQLSize(int columnSQLSize) {
        this.columnSQLSize = columnSQLSize;
    }

    /**
     * Checks if the current column is unique. If it is true it indicates that the column will be
     * Unique.
     * @return a <code>boolean</code>
     */
    public boolean isUnique() {
        return this.unique;
    }

    /**
     * Sets the value of the parameter unique. If it is true it indicates that the column will be
     * unique.
     * @param unique A boolean that sets the value of the parameter unique.
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
