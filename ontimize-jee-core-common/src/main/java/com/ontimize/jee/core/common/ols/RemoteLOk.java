package com.ontimize.jee.core.common.ols;

import java.rmi.Remote;

public interface RemoteLOk extends Remote {

    public boolean ok(int sessionId) throws Exception;

    public boolean ok(int sessionId, String number) throws Exception;

    public boolean isDevelopementL(int sessionId) throws Exception;

    public String getLValue(int sessionId, String name) throws Exception;

    public String getLContent(int sessionId) throws Exception;

    public Object getLInfoObject(int sessionId) throws Exception;

}
