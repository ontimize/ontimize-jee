package com.ontimize.jee.desktopclient.locator.handlers;

import java.util.Hashtable;

import com.ontimize.jee.common.db.EntityResult;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.common.security.ClientPermissionManager;

/**
 * The Class ClientPermissionInvocationDelegate.
 */
public class ClientPermissionInvocationDelegate extends AbstractInvocationDelegate implements ClientPermissionManager {

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.security.ClientPermissionManager#getClientPermissions(java.util.Hashtable, int)
     */
    @Override
    public EntityResult getClientPermissions(Hashtable userKeys, int sessionId) throws Exception {
        // TODO
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.security.ClientPermissionManager#installClientPermissions(java.util.Hashtable,
     * int)
     */
    @Override
    public void installClientPermissions(Hashtable userKeys, int sessionId) throws Exception {
        // TODO

    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.security.ClientPermissionManager#getTime()
     */
    @Override
    public long getTime() throws Exception {
        return System.currentTimeMillis();
    }

}
