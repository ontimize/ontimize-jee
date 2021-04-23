package com.ontimize.jee.core.common.gui.i18n;

import java.rmi.Remote;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public interface IDatabaseBundleManager extends Remote {

    /**
     * Get a list with all the available bundle classes
     * @param sessionId
     * @return
     * @throws Exception
     */
    public DatabaseBundleDescriptor[] getAvailableBundles(int sessionId) throws Exception;

    /**
     * Get the resource bundle for the specified name and locale
     * @param baseName
     * @param l
     * @param sessionId
     * @return
     * @throws Exception
     */
    public ResourceBundle getBundle(String baseName, Locale l, int sessionId) throws Exception;

    /**
     * Get all the resources for the specified locale and different names
     * @param baseNames A list of String objects with the names of the resources to query
     * @param l Locale
     * @param sessionId
     * @return
     * @throws Exception
     */
    public Map getBundles(List baseNames, Locale l, int sessionId) throws Exception;

    /**
     * Get a Map with all the available bundles in the database for the specified locale
     * @param l Locale
     * @param sessionId
     * @return The keys of the object are Strings with the bundle class name (for example
     *         com.ontimize.gui.i18m.bundle) and each value is the ResourceBundle object
     * @throws Exception
     */
    public Map getAllResourceBundles(Locale l, int sessionId) throws Exception;

    /**
     * Get a list with the suffix of the available locales in the database
     * @param sessionId
     * @return
     * @throws Exception
     */
    public String[] getAvailableLocales(int sessionId) throws Exception;

    /**
     * Update the specified values in the database bundle
     * @param values
     * @param sessionId
     * @throws Exception
     */
    public void updateBundleValues(DatabaseBundleValues values, int sessionId) throws Exception;

    /**
     * Delete the specified key and its translations values in the database bundle
     * @param values
     * @param sessionId
     * @throws Exception
     */
    public void deleteBundleValues(DatabaseBundleValues values, int sessionId) throws Exception;

}
