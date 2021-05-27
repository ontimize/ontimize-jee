package com.ontimize.jee.common.db;

import java.io.Serializable;
import java.sql.Types;

/**
 * Represents a null value as field content. It indicates the sql data type to use in the setNull
 * statement method.
 */

public class NullValue implements Serializable {

    private int sQLType = Types.VARCHAR;

    public NullValue(int sqlDataType) {
        this.sQLType = sqlDataType;
    }

    public NullValue() {
    }

    public int getSQLDataType() {
        return this.sQLType;
    }

    @Override
    public String toString() {
        return "";
    }

}
