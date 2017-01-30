package com.ontimize.jee.common.services.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.ontimize.gui.i18n.DatabaseBundleDescriptor;
import com.ontimize.gui.i18n.DatabaseBundleValues;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * The Interface IBundleService.
 */
public interface II18nService {

	/**
	 * Get a list with all the available bundle classes.
	 *
	 * @return the available bundles
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	DatabaseBundleDescriptor[] getAvailableBundles() throws OntimizeJEERuntimeException;

	/**
	 * Get the resource bundle for the specified name and locale.
	 *
	 * @param baseName
	 *            the base name
	 * @param locale
	 *            the locale
	 * @return the bundle
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	ResourceBundle getBundle(String baseName, Locale locale) throws OntimizeJEERuntimeException;

	/**
	 * Get all the resources for the specified locale and different names.
	 *
	 * @param baseNames
	 *            A list of String objects with the names of the resources to query
	 * @param locale
	 *            the locale
	 * @return the bundles
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	Map<String, ResourceBundle> getBundles(List<String> baseNames, Locale locale) throws OntimizeJEERuntimeException;

	/**
	 * Get a hashtable with all the available bundles in the database for the specified locale.
	 *
	 * @param locale
	 *            the locale
	 * @return The keys of the object are Strings with the bundle class name (for example com.ontimize.gui.i18m.bundle) and each value is the
	 *         ResourceBundle object
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	Map<String, ResourceBundle> getAllResourceBundles(Locale locale) throws OntimizeJEERuntimeException;

	/**
	 * Get a list with the suffix of the available locales in the database.
	 *
	 * @return the available locales
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	String[] getAvailableLocales() throws OntimizeJEERuntimeException;

	/**
	 * Update the specified values in the database bundle.
	 *
	 * @param values
	 *            the values
	 * @throws OntimizeJEERuntimeException
	 *             the ontimize jee runtime exception
	 */
	void updateBundleValues(DatabaseBundleValues values) throws OntimizeJEERuntimeException;

}
