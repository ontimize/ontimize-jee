package com.ontimize.jee.desktopclient.locator.handlers;

import com.ontimize.jee.common.db.EntityResult;
import com.ontimize.jee.common.gui.ConnectionOptimizer;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;

/**
 * The Class ConnectionOptimizerInvocationDelegate.
 */
public class ConnectionOptimizerInvocationDelegate extends AbstractInvocationDelegate implements ConnectionOptimizer {

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.gui.ConnectionOptimizer#testConnectionSpeed(int, boolean)
     */
    @Override
    public EntityResult testConnectionSpeed(int sizeInBytes, boolean compressed) throws Exception {
        return new EntityResult();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.gui.ConnectionOptimizer#setDataCompressionThreshold(java.lang.String, int, int)
     */
    @Override
    public void setDataCompressionThreshold(String user, int id, int compression) throws Exception {
        // do nothing right now
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.gui.ConnectionOptimizer#getDataCompressionThreshold(int)
     */
    @Override
    public int getDataCompressionThreshold(int sessionId) throws Exception {
        return 1;
    }

}
