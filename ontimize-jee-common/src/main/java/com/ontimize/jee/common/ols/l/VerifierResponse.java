package com.ontimize.jee.common.ols.l;

import java.io.Serializable;

public class VerifierResponse implements Serializable {

    private int code = -1;

    private Object value = null;

    private String message = null;

    public VerifierResponse(int code, Object value, String message) {
        this.code = code;
        this.value = value;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public Object getValue() {
        return this.value;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Code: " + this.code);
        sb.append(" MSG: " + (this.message != null ? this.message : "MESSAGE_NULL"));
        sb.append(" VALUE: " + (this.value != null ? this.value : "NULL_VALUE"));
        return sb.toString();
    }

}
