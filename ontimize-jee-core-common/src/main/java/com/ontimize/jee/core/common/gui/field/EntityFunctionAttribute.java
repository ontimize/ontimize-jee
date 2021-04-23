package com.ontimize.jee.core.common.gui.field;

import java.io.Serializable;
import java.util.Vector;

public class EntityFunctionAttribute implements Serializable {

    /**
     * String to identify the attribute
     */
    protected String attr;

    /**
     * Name of the entity to make the query
     */
    protected String entityName;

    /**
     * Name of the column to apply the function
     */
    protected String columName;

    /**
     * Column names to use as filter
     */
    protected Vector parentkeys;

    /**
     * Function name (max, min, count(*), sum, avg ...)
     */
    protected String function;

    /**
     * Column names to use in the select statement
     */
    protected Vector queryColumns;

    /**
     * Boolean to know if it is necessary use NullValues when parentkey columns have no value
     */
    protected boolean useNullValueToParentkeys = false;

    /**
     * By default the query other entities in EntityTable class always make a filter with the entity
     * key.<br>
     * If this parameter is false query not use the entity key as a filter parameter.
     */
    protected boolean useDefaultKeyFilter = true;

    /**
     * Creates a new EntityFunctionAttribute instance
     * @param attr Attribute identifier
     * @param entity Entity name
     * @param column Name of the column to apply the function in
     * @param filter List of columns to use as filter in the query statement
     * @param functionName Name of the function (avg, sum, max, min, count(*) ...)
     */
    public EntityFunctionAttribute(String attr, String entity, String column, Vector parentkeys, String functionName) {
        this.attr = attr;
        this.entityName = entity;
        this.columName = column;
        this.parentkeys = parentkeys;
        this.function = functionName;
    }

    /**
     * Gets the name of the entity
     * @return
     */
    public String getEntityName() {
        return this.entityName;
    }

    /**
     * Sets the entity name
     * @param entityName
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Get the name of the columns to apply the function in
     * @return
     */
    public String getColumn() {
        return this.columName;
    }

    /**
     * Sets the column names to use in the select statement
     * @param column
     */
    public void setCols(String column) {
        this.columName = column;
    }

    /**
     * Gets the name of the filter columns
     * @return
     */
    public Vector getParentkeys() {
        return this.parentkeys;
    }

    /**
     * Sets the column to use as a filter in the select statement
     * @param filter
     */
    public void setParentkeys(Vector filter) {
        this.parentkeys = filter;
    }

    /**
     * Gets the identifier of the attribute
     * @return
     */
    public String getAttr() {
        return this.attr;
    }

    /**
     * Sets the identifier of the attribute
     * @param attr
     */
    public void setAttr(String attr) {
        this.attr = attr;
    }

    /**
     * Checks if the query must use NullValue objects when filter columns value is null
     * @return
     */
    public boolean isUseNullValueToParentkeys() {
        return this.useNullValueToParentkeys;
    }

    /**
     * Sets the value of the useNullValueToParentkeys variable
     * @param useNullValueToParentkeys
     */
    public void setUseNullValueToParentkeys(boolean useNullValueToParentkeys) {
        this.useNullValueToParentkeys = useNullValueToParentkeys;
    }

    /**
     * Returns false if the query must not use the form key as filter. By default return true
     * @return
     */
    public boolean isUseDefaultKeyFilter() {
        return this.useDefaultKeyFilter;
    }

    /**
     * Sets the value of useDefaultKeyFilter parameter
     * @param useDefaultKeyFilter
     */
    public void setUseDefaultKeyFilter(boolean useDefaultKeyFilter) {
        this.useDefaultKeyFilter = useDefaultKeyFilter;
    }

    /**
     * Gets the function name
     * @return
     */
    public String getFunction() {
        return this.function;
    }

    /**
     * Sets the function name
     * @param function
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     * Gets the string to use the function in the select statement with the necessary alias<br>
     * For example if function is max this returns "max(functionColumn) as attribute"
     * @return
     */
    public String getFunctionStringQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.function);
        if (this.columName != null) {
            sb.append("(" + this.columName + ")");
        }

        sb.append(" as \"" + this.attr + "\"");

        return sb.toString();
    }

    /**
     * Gets the column names to use in the query
     * @return
     */
    public Vector getQueryColumns() {
        return this.queryColumns;
    }

    /**
     * Sets the name of the columns to use in the query
     * @param queryColumns
     */
    public void setQueryColumns(Vector queryColumns) {
        this.queryColumns = queryColumns;
    }

    @Override
    public String toString() {
        // return this.getClass().getName() + "." + this.attr;
        return this.attr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityFunctionAttribute) {
            EntityFunctionAttribute comparate = (EntityFunctionAttribute) obj;

            if (!comparate.getAttr().equals(this.getAttr())) {
                return false;
            }

            if (!comparate.getFunction().equals(this.getFunction())) {
                return false;
            }

            if (!comparate.getEntityName().equals(this.getEntityName())) {
                return false;
            }

            if ((this.getFunctionStringQuery() != null)
                    && !this.getFunctionStringQuery().equals(comparate.getFunctionStringQuery())) {
                return false;
            }

            if ((this.getFunctionStringQuery() == null) && (comparate.getFunctionStringQuery() != null)) {
                return false;
            }

            if (this.getQueryColumns() != null) {
                Vector v = comparate.getQueryColumns();
                if (v == null) {
                    return false;
                } else {
                    if (v.size() != this.getQueryColumns().size()) {
                        return false;
                    } else {
                        for (int i = 0; i < this.getQueryColumns().size(); i++) {
                            if (!v.contains(this.getQueryColumns().get(i))) {
                                return false;
                            }
                        }
                    }
                }
            } else {
                if (comparate.getQueryColumns() != null) {
                    return false;
                }
            }

            if (this.getParentkeys() != null) {
                Vector v = comparate.getParentkeys();
                if (v == null) {
                    return false;
                } else {
                    if (v.size() != this.getParentkeys().size()) {
                        return false;
                    } else {
                        for (int i = 0; i < this.getParentkeys().size(); i++) {
                            if (!v.contains(this.getParentkeys().get(i))) {
                                return false;
                            }
                        }
                    }
                }
            } else {
                if (comparate.getParentkeys() != null) {
                    return false;
                }
            }

            return true;

        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int i = 0;
        if (this.attr != null) {
            i += this.attr.hashCode();
        }

        if (this.function != null) {
            i += this.function.hashCode();
        }

        if (this.columName != null) {
            i += this.columName.hashCode();
        }

        if (this.queryColumns != null) {
            for (int j = 0; j < this.queryColumns.size(); j++) {
                if (this.queryColumns.get(j) != null) {
                    i += this.queryColumns.get(j).hashCode();
                }
            }
        }

        if (this.parentkeys != null) {
            for (int j = 0; j < this.parentkeys.size(); j++) {
                if (this.parentkeys.get(j) != null) {
                    i += this.parentkeys.get(j).hashCode();
                }
            }
        }

        if (this.entityName != null) {
            i += this.entityName.hashCode();
        }

        if (this.isUseNullValueToParentkeys()) {
            i += Boolean.TRUE.hashCode();
        } else {
            i += Boolean.FALSE.hashCode();
        }

        return i;
    }

}
