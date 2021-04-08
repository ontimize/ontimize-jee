package com.ontimize.jee.desktopclient.locator.handlers;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.ontimize.gui.i18n.DatabaseBundleDescriptor;
import com.ontimize.gui.i18n.DatabaseBundleValues;
import com.ontimize.gui.i18n.IDatabaseBundleManager;
import com.ontimize.jee.common.services.i18n.II18nService;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.common.tools.proxy.AbstractInvocationDelegate;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class ClientPermissionInvocationDelegate.
 */
public class DatabaseBundleManagerInvocationDelegate extends AbstractInvocationDelegate
        implements IDatabaseBundleManager {

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.IDatabaseBundleManager#getAvailableBundles(int)
     */
    @Override
    public DatabaseBundleDescriptor[] getAvailableBundles(int sessionId) throws Exception {
        return this.getI18nService().getAvailableBundles();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.IDatabaseBundleManager#getBundle(java.lang.String, java.util.Locale,
     * int)
     */
    @Override
    public ResourceBundle getBundle(String baseName, Locale locale, int sessionId) throws Exception {
        return this.getI18nService().getBundle(baseName, locale);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.IDatabaseBundleManager#getBundles(java.util.List, java.util.Locale,
     * int)
     */
    @Override
    public Hashtable getBundles(List baseNames, Locale locale, int sessionId) throws Exception {
        return new Hashtable<>(this.getI18nService().getBundles(baseNames, locale));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.IDatabaseBundleManager#getAllResourceBundles(java.util.Locale, int)
     */
    @Override
    public Hashtable getAllResourceBundles(Locale locale, int sessionId) throws Exception {
        return new Hashtable<>(this.getI18nService().getAllResourceBundles(locale));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.IDatabaseBundleManager#getAvailableLocales(int)
     */
    @Override
    public String[] getAvailableLocales(int sessionId) throws Exception {
        return this.getI18nService().getAvailableLocales();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.IDatabaseBundleManager#updateBundleValues(com.ontimize.gui.i18n.
     * DatabaseBundleValues, int)
     */
    @Override
    public void updateBundleValues(DatabaseBundleValues values, int sessionId) throws Exception {
        this.getI18nService().updateBundleValues(values);
    }

    /**
     * Gets the i18n service.
     * @return the i18n service
     */
    private II18nService getI18nService() {
        II18nService i18nService = BeansFactory.getBean(II18nService.class);
        CheckingTools.failIfNull(i18nService, "No i18nservice configured");
        return i18nService;
    }

    @Override
    public void deleteBundleValues(DatabaseBundleValues values, int sessionId) throws Exception {
        this.getI18nService().deleteBundleValues(values);

    }

}
