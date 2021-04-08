package com.ontimize.jee.desktopclient.locator.handlers;

import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.ols.RemoteLOk;

/**
 * The Class LicenseLocatorInvocationDelegate manages License stuffs related with RemoteLocator.
 */
public class LicenseLocatorInvocationDelegate extends AbstractInvocationDelegate implements RemoteLOk {

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.ols.RemoteLOk#getLContent(int)
     */
    @Override
    public String getLContent(int sessionId) throws Exception {
        return "OK";
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.ols.RemoteLOk#getLInfoObject(int)
     */
    @Override
    public Object getLInfoObject(int sessionId) throws Exception {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.ols.RemoteLOk#getLValue(int, java.lang.String)
     */
    @Override
    public String getLValue(int sessionId, String name) throws Exception {
        return "OK";
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.ols.RemoteLOk#isDevelopementL(int)
     */
    @Override
    public boolean isDevelopementL(int sessionId) throws Exception {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.ols.RemoteLOk#ok(int)
     */
    @Override
    public boolean ok(int sessionId) throws Exception {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.ols.RemoteLOk#ok(int, java.lang.String)
     */
    @Override
    public boolean ok(int sessionId, String number) throws Exception {
        return true;
    }

}
