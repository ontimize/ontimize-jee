package com.ontimize.jee.desktopclient.locator.preferences;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.preferences.RemoteApplicationPreferences;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.preferences.IRemoteApplicationPreferencesService;
import com.ontimize.jee.common.tools.ReflectionTools;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.i18n.I18NNaming;
import com.ontimize.jee.desktopclient.locator.OJeeClientPermissionLocator;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class RemoteApplicationPreferencesInvocationHandler.
 */
public class RemoteApplicationPreferencesInvocationHandler extends AbstractInvocationDelegate
        implements RemoteApplicationPreferences {

    private static final Logger logger = LoggerFactory.getLogger(RemoteApplicationPreferencesInvocationHandler.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The preferences service. */
    private IRemoteApplicationPreferencesService preferencesService = null;

    private final BlockingQueue<Operation> queue;

    private final Thread consummerThread;

    public RemoteApplicationPreferencesInvocationHandler() {
        super();
        this.queue = new LinkedBlockingQueue<>();
        this.consummerThread = new Thread("RemoteApplicationPreferences-Thread") {

            @Override
            public void run() {

                while (true) {
                    try {
                        Operation op = RemoteApplicationPreferencesInvocationHandler.this.queue.take();
                        ReflectionTools.invoke(
                                RemoteApplicationPreferencesInvocationHandler.this.getRemotePreferencesService(),
                                op.getMethod(), op.getParameters());
                    } catch (Exception error) {
                        RemoteApplicationPreferencesInvocationHandler.logger.error(null, error);
                    }
                }
            };
        };
        this.consummerThread.start();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.preferences.RemoteApplicationPreferences#getRemotePreference(int,
     * java.lang.String, java.lang.String)
     */
    @Override
    public String getRemotePreference(int sessionId, String user, String preferenceName) throws Exception {
        if (user == null) {
            return this.getRemotePreferencesService().getDefaultPreference(preferenceName);
        } else {
            return this.getRemotePreferencesService().getPreference(preferenceName);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.preferences.RemoteApplicationPreferences#setRemotePreference(int,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setRemotePreference(int sessionId, String user, String preferenceName, String value) throws Exception {
        if (user == null) {
            this.queue.put(new Operation("setDefaultPreference", new Object[] { preferenceName, value }));
        } else {
            this.queue.put(new Operation("setPreference", new Object[] { preferenceName, value }));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.preferences.RemoteApplicationPreferences#saveRemotePreferences(int)
     */
    @Override
    public void saveRemotePreferences(int sessionId) throws Exception {
        if (!this.isLogged()) {
            return;
        }
        this.getRemotePreferencesService().savePreferences();
    }

    private boolean isLogged() {
        return ((OJeeClientPermissionLocator) ApplicationManager.getApplication().getReferenceLocator())
            .getUserInformation() != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.preferences.RemoteApplicationPreferences#loadRemotePreferences(int)
     */
    @Override
    public void loadRemotePreferences(int sessionId) throws Exception {
        if (!this.isLogged()) {
            return;
        }
        this.getRemotePreferencesService().loadPreferences();
    }

    /**
     * Gets the remote preferences service.
     * @return the remote preferences service
     */
    protected IRemoteApplicationPreferencesService getRemotePreferencesService() {
        if (this.preferencesService == null) {
            this.preferencesService = BeansFactory.getBean(IRemoteApplicationPreferencesService.class);
        }
        if (this.preferencesService == null) {
            throw new OntimizeJEERuntimeException(I18NNaming.E_REMOTE_PREFERENCES_SERVICE_NOT_FOUND);
        }
        return this.preferencesService;
    }

    private static class Operation {

        private final String method;

        private final Object[] parameters;

        public Operation(String method, Object[] parameters) {
            super();
            this.method = method;
            this.parameters = parameters;
        }

        public String getMethod() {
            return this.method;
        }

        public Object[] getParameters() {
            return this.parameters;
        }

    }

}
