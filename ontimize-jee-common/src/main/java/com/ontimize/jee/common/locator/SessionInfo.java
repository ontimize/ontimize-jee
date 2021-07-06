package com.ontimize.jee.common.locator;

public class SessionInfo implements java.io.Serializable {

    protected int sessionId = -1;

    protected String description = "";

    public SessionInfo(int sessionId, String descr) {
        this.sessionId = sessionId;
        this.description = descr;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public int getSessionId() {
        return this.sessionId;
    }

}
