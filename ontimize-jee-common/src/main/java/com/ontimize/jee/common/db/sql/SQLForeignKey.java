package com.ontimize.jee.common.db.sql;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class SQLForeignKey extends SQLConstraint {

    protected static final String FOREIGN_KEY = "FOREIGN KEY ";

    protected static final String REFERENCES = "REFERENCES ";

    /**
     * The name of the referenced table
     */
    protected String secondaryTable;

    /**
     * The list of columns of the referenced table in where the constraint is applied.
     */
    protected List foreignColumns = new ArrayList();

    /**
     * This method returns the list of column/s of the referenced table that are going to be used into
     * the constraint.<br>
     * @return a <code>String</code> with the column/s between parenthesis
     */
    public List getForeignColumns() {
        return this.foreignColumns;
    }

    /**
     * @param secondaryTable The table, in which the restriction is made,
     * @param keys Must contains a pair key-value, where key is the table column and value is the name
     *        of reference column.
     */
    public SQLForeignKey(String secondaryTable, Map keys) {
        this.secondaryTable = secondaryTable;
        this.setKeys(keys);
    }

    /**
     * @param constraintName The name of the constraint.
     * @param secondaryTable The table, in which the restriction is made,
     * @param keys Must contains a pair key-value, where key is the table column and value is the name
     *        of reference column.
     */
    public SQLForeignKey(String constraintName, String secondaryTable, Map keys) {
        this.constraintName = constraintName;
        this.secondaryTable = secondaryTable;
        this.setKeys(keys);
    }

    /**
     * @param constraintName The name of the constraint.
     */
    public SQLForeignKey(String constraintName) {
        this.constraintName = constraintName;
    }

    /**
     * @param keys Must contains a pair key-value, where key is the table column and value is the name
     *        of reference column.
     */
    public void setKeys(Map keys) {
        this.foreignColumns.clear();
        this.columns.clear();
        Enumeration enumeration = Collections.enumeration(keys.keySet());
        while (enumeration.hasMoreElements()) {
            Object key = enumeration.nextElement();
            Object value = keys.get(key);
            this.foreignColumns.add(value);
            this.columns.add(key);
        }
    }

    /**
     * Returns the name of the referenced table. The table, in which the restriction is made.
     * @return a <code>String</code> with the name of the referenced table.
     */
    public String getSecondaryTable() {
        return this.secondaryTable;
    }

    /**
     * This method sets the name of the referenced table of the Foreign key.
     * @param secondaryTable String with the name of the referenced table.
     */
    public void setSecondaryTable(String secondaryTable) {
        this.secondaryTable = secondaryTable;
    }

    /**
     * This method returns the SQL Statement to add a Foreign Key constraint to a database table.
     * Returns a <code>String<code> with the SQL Statement
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        if (this.getConstraintName() != null) {
            buffer.append(CONSTRAINT);
            buffer.append(this.getConstraintName()).append(" ");
        }

        buffer.append(SQLForeignKey.FOREIGN_KEY).append(" ");

        boolean equal = true;

        StringBuilder keyBuffer = new StringBuilder();
        StringBuilder valuesBuffer = new StringBuilder();
        keyBuffer.append(SQLHandler.OPEN_PARENTHESIS);
        valuesBuffer.append(SQLHandler.OPEN_PARENTHESIS);

        boolean coma = false;
        for (int i = 0; i < this.columns.size(); i++) {
            if (coma) {
                keyBuffer.append(",");
                valuesBuffer.append(",");
            } else {
                coma = true;
            }
            String key = (String) this.columns.get(i);
            String value = (String) this.foreignColumns.get(i);
            if (!key.equals(value)) {
                equal = false;
            }
            keyBuffer.append(key);
            valuesBuffer.append(value);
        }

        keyBuffer.append(SQLHandler.CLOSE_PARENTHESIS);
        valuesBuffer.append(SQLHandler.CLOSE_PARENTHESIS);

        buffer.append(keyBuffer.toString());
        buffer.append(" ");
        buffer.append(SQLForeignKey.REFERENCES);
        buffer.append(" ");
        buffer.append(this.getSecondaryTable());
        buffer.append(" ");
        if (!equal) {
            buffer.append(valuesBuffer.toString());
        }
        return buffer.toString();
    }

}
