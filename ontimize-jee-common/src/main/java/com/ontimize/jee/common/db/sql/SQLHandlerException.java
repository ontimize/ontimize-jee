package com.ontimize.jee.common.db.sql;

public class SQLHandlerException extends Exception {

    /**
     * ISQLInfo object that contains the whole SQL information(error operation, executed statements...)
     */
    protected ISQLInfo info = null;

    /**
     * Constructs a new SQLHandlerException with the specified detail SQL information and cause
     * @param t The cause (which is saved for later retrieval by the Throwable.getCause() method).
     * @param info ISQLInfo object with the whole SQL information.
     */
    public SQLHandlerException(Throwable t, ISQLInfo info) {
        super(t);
        this.info = info;
    }

    /**
     * Returns the SQL information.
     * @return a <code>ISQLInfo</code>
     */
    public ISQLInfo getSQLInfo() {
        return this.info;
    }

}
