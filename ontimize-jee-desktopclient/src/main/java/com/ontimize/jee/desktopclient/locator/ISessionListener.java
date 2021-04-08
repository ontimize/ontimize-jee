package com.ontimize.jee.desktopclient.locator;

public interface ISessionListener {

    void sessionStarted(int sessionId);

    void sessionClosed(int sessionId);

}
