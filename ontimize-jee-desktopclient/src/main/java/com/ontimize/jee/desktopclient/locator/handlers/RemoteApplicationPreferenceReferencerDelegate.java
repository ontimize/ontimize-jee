package com.ontimize.jee.desktopclient.locator.handlers;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ontimize.gui.preferences.RemoteApplicationPreferenceReferencer;
import com.ontimize.gui.preferences.RemoteApplicationPreferences;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.locator.preferences.RemoteApplicationPreferencesInvocationHandler;

/**
 * The Class RemoteApplicationPreferenceReferencerDelegate.
 */
public class RemoteApplicationPreferenceReferencerDelegate extends AbstractInvocationDelegate
        implements RemoteApplicationPreferenceReferencer {

    /** The preferences manager. */
    private RemoteApplicationPreferences preferencesManager;

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.preferences.RemoteApplicationPreferenceReferencer#
     * getRemoteApplicationPreferences(int)
     */
    @Override
    public RemoteApplicationPreferences getRemoteApplicationPreferences(int sessionId) throws Exception {
        if (this.preferencesManager == null) {
            Class<?>[] interfacesExtending = ReflectionTools.getInterfacesExtending(RemoteApplicationPreferences.class);
            List<Class<?>> interfaces = new ArrayList<>(Arrays.asList(interfacesExtending));
            interfaces.add(RemoteApplicationPreferences.class);
            interfacesExtending = interfaces.toArray(new Class<?>[0]);

            this.preferencesManager = (RemoteApplicationPreferences) Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(), interfacesExtending,
                    new RemoteApplicationPreferencesInvocationHandler());
        }
        return this.preferencesManager;
    }

}
