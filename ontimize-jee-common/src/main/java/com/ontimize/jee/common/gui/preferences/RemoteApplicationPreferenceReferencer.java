package com.ontimize.jee.common.gui.preferences;

public interface RemoteApplicationPreferenceReferencer extends java.rmi.Remote {

    /**
     * Get the application preferences stored in the server side
     * @param sessionId User session identifier
     * @return
     * @throws Exception
     */
    public RemoteApplicationPreferences getRemoteApplicationPreferences(int sessionId) throws Exception;

}
