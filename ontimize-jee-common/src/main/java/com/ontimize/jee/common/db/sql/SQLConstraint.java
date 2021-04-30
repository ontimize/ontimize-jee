package com.ontimize.jee.common.db.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to define the constraint
 */

public abstract class SQLConstraint implements Serializable {

    public static final String CONSTRAINT = "CONSTRAINT ";

    /**
     * Name of the constraint
     */
    protected String constraintName;

    /**
     * The list of columns in where the constraint is applied.
     */
    protected List columns = new ArrayList();

    public List getColumns() {
        return this.columns;
    }

    /**
     * Return the constraint name.
     * @return a <code>String</code> with the constraint name.
     */
    public String getConstraintName() {
        return this.constraintName;
    }

    /**
     * Sets the constraint name
     * @param constraintName Name of the constraint.
     */
    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    /**
     * This method returns the list of column/s that are going to be used into the constraint.<br>
     * @return a <code>String</code> with the column/s between parenthesis
     */
    public String getColumnList() {
        StringBuilder keyBuffer = new StringBuilder();

        keyBuffer.append(SQLHandler.OPEN_PARENTHESIS);
        List columns = this.getColumns();
        boolean coma = false;
        for (int i = 0; i < columns.size(); i++) {
            if (coma) {
                keyBuffer.append(",");
            } else {
                coma = true;
            }
            keyBuffer.append((String) columns.get(i));
        }
        keyBuffer.append(SQLHandler.CLOSE_PARENTHESIS);

        return keyBuffer.toString();
    }

}
