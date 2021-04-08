package com.ontimize.jee.desktopclient.locator.handlers;

import java.util.Hashtable;

import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.util.remote.BytesBlock;
import com.ontimize.xml.XMLClientProvider;

/**
 * The Class XMLClientProviderInvocationDelegate.
 */
public class XMLClientProviderInvocationDelegate extends AbstractInvocationDelegate implements XMLClientProvider {

    @Override
    public String getXMLForm(String form, int userid) throws Exception {
        return null;
    }

    @Override
    public Hashtable getFormManagerParameters(String formManagerId, int userid) throws Exception {
        return null;
    }

    @Override
    public String getXMLRules(String form, int userid) throws Exception {
        return null;
    }

    @Override
    public String getXMLMenu(int userid) throws Exception {
        return null;
    }

    @Override
    public void reloadXMLMenu(int userId) throws Exception {
        // Do nothing
    }

    @Override
    public String getXMLToolbar(int userid) throws Exception {
        return null;
    }

    @Override
    public void reloadXMLToolbar(int userId) throws Exception {
        // Do nothing
    }

    @Override
    public BytesBlock getImage(String image, int userId) throws Exception {
        return null;
    }

}
