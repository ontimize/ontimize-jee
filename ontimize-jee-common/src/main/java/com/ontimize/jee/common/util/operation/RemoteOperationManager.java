package com.ontimize.jee.common.util.operation;

import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Imatia Innovation S.L.
 * @since 5.2008
 */

public interface RemoteOperationManager extends Remote {

    public static final int WAITING = 0;

    public static final int WAITING_REQUIRED = 1;

    public static final int RUNNING = 2;

    public static final int FINISHED = 3;

    public static final int UNKNOWN = -1;

    public String run(String clase, HashMap parameters, int sessionId) throws Exception;

    public boolean hasRequired(String token, int sessionId) throws Exception;

    public HashMap getRequired(String token, int sessionId) throws Exception;

    public void setRequired(String token, HashMap required, int sessionId) throws Exception;

    public void cancel(String token, int sessionId) throws Exception;

    public boolean isFinished(String token, int sessionId) throws Exception;

    public Map getResult(String token, int sessionId) throws Exception;

    public int getStatus(String token, int sessionId) throws Exception;

    public Map getCurrentExecutionInformation(String token, int sessionId) throws Exception;

}
