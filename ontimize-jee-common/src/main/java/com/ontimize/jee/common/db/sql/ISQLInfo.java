package com.ontimize.jee.common.db.sql;

import java.util.List;

/**
 * Interface.
 *
 * @author Imatia Innovation
 */
public interface ISQLInfo {

    /**
     * Operation code that indicates that the operation was successfully.
     */
    public static final int OK = 0;

    /**
     * Operation code that indicates that an error have been occurred.
     */
    public static final int ERROR = -1;

    /**
     * This method obtains the code that indicates if the operation was successfully or not.
     * @return int
     */
    public int getCode();

    /**
     * In this method establishes the code of the operation.
     * @param code
     */
    public void setCode(int code);

    /**
     * This method returns the statement/s that has been executed.
     * @return List
     */
    public List getSQLStatements();

    /**
     * This method add the SQL Statement to the collection of statements executed.
     * @param statement String with the SQL Statement.
     */
    public void addSQLStatement(String statement);

    public void appendSQLInfo(ISQLInfo sqlInfo);

    /**
     * This method sets the SQL Statement like the one which causes the SQL Exception.
     * @param statement String with the SQL Statement.
     */
    public void setErrorSQLStatement(String statement);

    /**
     * This method returns the SQL Statement which causes the SQL Exception.
     * @return String
     */
    public String getErrorSQLStatement();

}
