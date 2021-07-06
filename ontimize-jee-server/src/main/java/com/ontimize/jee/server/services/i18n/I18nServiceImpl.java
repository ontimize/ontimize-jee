package com.ontimize.jee.server.services.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontimize.jee.common.gui.i18n.DatabaseBundleDescriptor;
import com.ontimize.jee.common.gui.i18n.DatabaseBundleValues;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.i18n.II18nService;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;

/**
 * The Class I18nServiceImpl.
 */
@Service("I18nService")
public class I18nServiceImpl implements II18nService, ApplicationContextAware {

    /** The engine. */
    private II18nService engine;

    /**
     * The Constructor.
     */
    public I18nServiceImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getAvailableBundles()
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DatabaseBundleDescriptor[] getAvailableBundles() throws OntimizeJEERuntimeException {
        return this.getEngine().getAvailableBundles();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getBundle(java.lang.String,
     * java.util.Locale)
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResourceBundle getBundle(String baseName, Locale locale) throws OntimizeJEERuntimeException {
        return this.getEngine().getBundle(baseName, locale);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getBundles(java.util.List,
     * java.util.Locale)
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, ResourceBundle> getBundles(List<String> baseNames, Locale locale)
            throws OntimizeJEERuntimeException {
        return this.getEngine().getBundles(baseNames, locale);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getAllResourceBundles(java.util.Locale)
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, ResourceBundle> getAllResourceBundles(Locale locale) throws OntimizeJEERuntimeException {
        return this.getEngine().getAllResourceBundles(locale);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getAvailableLocales()
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String[] getAvailableLocales() throws OntimizeJEERuntimeException {
        return this.getEngine().getAvailableLocales();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.common.services.i18n.II18nService#updateBundleValues(com.ontimize.jee.common.gui
     * .i18n. DatabaseBundleValues)
     */
    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBundleValues(DatabaseBundleValues values) throws OntimizeJEERuntimeException {
        this.getEngine().updateBundleValues(values);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#deleteBundleValues(com
     * .ontimize.gui.i18n.DatabaseBundleValues)
     */

    @Secured({ PermissionsProviderSecured.SECURED })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBundleValues(DatabaseBundleValues values) throws OntimizeJEERuntimeException {
        this.getEngine().deleteBundleValues(values);
    }

    /**
     * Sets the engine.
     * @param engine the engine
     */
    public void setEngine(II18nService engine) {
        this.engine = engine;
    }

    /**
     * Gets the engine.
     * @return the engine
     */
    public II18nService getEngine() {
        return this.engine;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.
     * context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.setEngine(applicationContext.getBean(OntimizeConfiguration.class).getI18nConfiguration().getEngine());
    }

}
