package com.ontimize.jee.core.common.ols;

import java.rmi.Remote;
import java.util.Map;

public interface RemoteLControlAdministration extends Remote {

    public Map getParameters(String login, String password) throws Exception;

    public Map updateL(Map h, String login, String password) throws Exception;

    public boolean ok(String login, String password) throws Exception;

}
