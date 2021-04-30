package com.ontimize.jee.common.util.sso;

import java.rmi.Remote;
import java.util.Map;

public interface ISSOServer extends Remote {

    public String getServerServiceName(String clientName) throws Exception;

    public byte[] returnMessage(String contextRef, byte[] inData) throws Exception;

    public Map getConfigurationParameters(String clientName) throws Exception;

}
