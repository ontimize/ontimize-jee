package com.ontimize.jee.core.common.locator;


import java.awt.Component;
import java.util.List;

/**
 * Interface that musts be implemented by the client locator
 */
public interface ClientReferenceLocator {

    /**
     * Get the time between two verifications of new chat messages
     * @return
     */
    public int getChatCheckTime();

    public int getMessageCheckTime();

    /**
     * True when the application has a chat configured
     * @return
     */
    public boolean hasChat();

    public void showMessageDialog(Component c);

    /**
     * Get the current user name
     * @return
     */
    public String getUser();

    public InitialContext getInitialContext();

    /**
     * Allows to get local references defined in 'references.xml' file <b> when app is built in local
     * mode. </b>. For remote references is used {@link #getRemoteReference(String, int)}
     * @param name The name of reference defined in 'references.xml' file.
     * @return Object for this reference
     * @throws Exception When application is not in local mode or when something fails.
     * @since 5.2073EN-0.2
     */
    public Object getReference(String name) throws Exception;

    public Object getLocaleId(int sessionId) throws Exception;

    public void addModuleMemoryEntity(String localEntityPackage, List<String> localEntities);

    public boolean isLocalMode();

    public boolean isAllowCertificateLogin();

}
