package com.ontimize.jee.server.services.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import com.ontimize.jee.common.dto.EntityResultMapImpl;
import org.springframework.beans.factory.InitializingBean;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.gui.i18n.DatabaseBundleDescriptor;
import com.ontimize.jee.common.gui.i18n.DatabaseBundleValues;
import com.ontimize.jee.common.gui.i18n.DatabaseBundleValues.BundleValue;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.i18n.II18nService;
import com.ontimize.jee.common.services.i18n.TmpDatabaseResourceBundle;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.DaoProperty;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.common.util.ParseTools;

/**
 * The Class DatabaseI18nEngine.
 */
public class DatabaseI18nEngine implements II18nService, InitializingBean {

    /** The bundle class name column. */
    private String bundleClassNameColumn = "BUNDLECLASSNAME";

    /** The bundle description column. */
    private String bundleDescriptionColumn = "DESCRIPTION";

    /** The bundle key column. */
    private String bundleKeyColumn = "IDBUNDLE";

    /** The bundle values key column. */
    private String bundleValuesKeyColumn = "IDBUNDLEVALUE";

    /** The bundle values text key column. */
    private String bundleValuesTextKeyColumn = "TEXTKEY";

    /** The dao bundle values. */
    private IOntimizeDaoSupport daoBundleValues;

    /** The dao bundles. */
    private IOntimizeDaoSupport daoBundles;

    /**
     * Boolean to know the names of the columns in the table<br>
     * Default value is null, to use directly the names in the configuration file or the default
     * ones.<br>
     * If it is true then use the values but in lower case. If it is false then use the configuration or
     * default values but in upper case
     */
    public Boolean toLowerCase = null;

    /** The locale column names. */
    private Map<String, String> localeColumnNames;

    /**
     * The Constructor.
     * @throws Exception the exception
     */
    public DatabaseI18nEngine() throws Exception {
        super();
    }

    /**
     * Autoconfigure locale columns.
     */
    protected void autoconfigureLocaleColumns() {
        List<DaoProperty> cudProperties = this.daoBundleValues.getCudProperties();

        List<String> noLocaleColumns = Arrays
            .asList(new String[] { this.bundleKeyColumn, this.bundleValuesKeyColumn, this.bundleValuesTextKeyColumn });

        if (this.localeColumnNames == null) {
            this.localeColumnNames = new HashMap<>();
        }
        for (DaoProperty property : cudProperties) {
            // The locale name must be the java value
            // language is in lower case and country in upper case
            if (!noLocaleColumns.contains(property.getPropertyName())) {
                String localeStr = this.getLocaleString(property.getPropertyName());
                if (localeStr != null) {
                    this.localeColumnNames.put(localeStr, property.getPropertyName());
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getAllResourceBundles(java.util.Locale)
     */
    @Override
    public Map<String, ResourceBundle> getAllResourceBundles(Locale locale) throws OntimizeJEERuntimeException {
        DatabaseBundleDescriptor[] availableBundles = this.getAvailableBundles();
        Map<String, ResourceBundle> result = new HashMap<>();
        for (DatabaseBundleDescriptor bundleDescriptor : availableBundles) {
            ResourceBundle bundle = this.getBundle(bundleDescriptor, locale);
            if (bundle != null) {
                result.put(bundleDescriptor.getBundleClassName(), bundle);
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getBundles(java.util.List,
     * java.util.Locale)
     */
    @Override
    public Map<String, ResourceBundle> getBundles(List<String> baseNames, Locale locale)
            throws OntimizeJEERuntimeException {
        Map<String, ResourceBundle> result = new HashMap<>();

        for (String baseName : baseNames) {
            ResourceBundle bundle = this.getBundle(baseName, locale);
            if (bundle != null) {
                result.put(baseName, bundle);
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getBundle(java.lang.String,
     * java.util.Locale)
     */
    @Override
    public ResourceBundle getBundle(String baseName, Locale locale) throws OntimizeJEERuntimeException {
        String localeColumn = this.getColumnName(locale);
        if (localeColumn != null) {
            DatabaseBundleDescriptor bundleDescriptor = this.getBundleDescriptor(baseName);
            return this.getBundle(bundleDescriptor, locale);
        }
        return null;
    }

    /**
     * Get all the available bundles in the database.<br>
     * If (DatabaseBundleManager.{@link #bundleValuesEntityName} is not null and a entity with this name
     * exists then use this entity to query the available bundles.<br>
     * If (DatabaseBundleManager.{@link #bundleValuesEntityName} is null and {@link #bundleTableName} is
     * not null then query the table directly. In other case return null
     * @return the available bundles
     * @throws OntimizeJEERuntimeException the ontimize jee runtime exception
     */
    @Override
    public DatabaseBundleDescriptor[] getAvailableBundles() throws OntimizeJEERuntimeException {

        EntityResult queryResult = this.daoBundles.query(new HashMap<>(),
                Arrays.asList(new String[] { this.bundleClassNameColumn, this.bundleDescriptionColumn,
                        this.bundleKeyColumn }),
                (List<String>) null, (String) null);

        int queryCount = queryResult.calculateRecordNumber();
        DatabaseBundleDescriptor[] bundles = new DatabaseBundleDescriptor[queryCount];
        for (int i = 0; i < queryCount; i++) {
            Map<?, ?> recordData = queryResult.getRecordValues(i);

            String bundleClassName = (String) recordData.get(this.bundleClassNameColumn);
            String bundleDescription = (String) recordData.get(this.bundleDescriptionColumn);
            Object bundleId = recordData.get(this.bundleKeyColumn);
            bundles[i] = new DatabaseBundleDescriptor(bundleId, bundleClassName, bundleDescription);
        }
        return bundles;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#getAvailableLocales()
     */
    @Override
    public String[] getAvailableLocales() throws OntimizeJEERuntimeException {
        if (this.localeColumnNames != null) {
            Object[] array = this.localeColumnNames.keySet().toArray();
            String[] result = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = (String) array[i];
            }
            return result;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.common.services.i18n.II18nService#updateBundleValues(com.ontimize.jee.common.gui
     * .i18n. DatabaseBundleValues)
     */
    @Override
    public void updateBundleValues(DatabaseBundleValues dbvalues) throws OntimizeJEERuntimeException {
        List<BundleValue> bundleValues = dbvalues.getBundleValues();
        DatabaseBundleDescriptor[] availableBundlesArray = this.getAvailableBundles();
        List<DatabaseBundleDescriptor> availableBundles = new ArrayList<>(Arrays.asList(availableBundlesArray));

        Map<String, Object> filter = new HashMap<>();
        for (int i = 0; i < bundleValues.size(); i++) {
            BundleValue bv = bundleValues.get(i);
            Object bundleId = this.getBundleId(bv.getBundleClassName(), availableBundles);
            // Use the bundleId and textKey as unique key to update each
            // bundle value
            filter.clear();
            filter.put(this.bundleKeyColumn, bundleId);
            filter.put(this.bundleValuesTextKeyColumn, bundleValues.get(i).getTextKey());

            Map<String, Object> htValues = bundleValues.get(i).getTranslationValues();
            Map<String, Object> hValues = new HashMap<>();
            if (htValues != null) {
                for (Entry<String, Object> entry : htValues.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if ((value != null) && (value instanceof String) && (((String) value).trim().length() > 0)) {
                        hValues.put(this.localeColumnNames.get(key), value);
                    } else if (!(value instanceof String)) {
                        hValues.put(this.localeColumnNames.get(key), value);
                    }
                }
            }

            Object key = this.getBundleValueKey(filter);
            if (key != null) {
                // update
                filter.put(this.bundleValuesKeyColumn, key);
                this.daoBundleValues.update(hValues, filter);
            } else {
                // insert
                hValues.putAll(filter);
                this.daoBundleValues.insert(hValues);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.common.services.i18n.II18nService#deleteBundleValues(com
     * .ontimize.gui.i18n.DatabaseBundleValues)
     */
    @Override
    public void deleteBundleValues(DatabaseBundleValues dbvalues) throws OntimizeJEERuntimeException {
        List<BundleValue> bundleValues = dbvalues.getBundleValues();
        DatabaseBundleDescriptor[] availableBundlesArray = this.getAvailableBundles();
        List<DatabaseBundleDescriptor> availableBundles = new ArrayList<>(Arrays.asList(availableBundlesArray));

        Map filter = new HashMap();
        for (int i = 0; i < bundleValues.size(); i++) {

            BundleValue bv = bundleValues.get(i);
            Object bundleId = this.getBundleId(bv.getBundleClassName(), availableBundles);

            filter.clear();
            filter.put(this.bundleKeyColumn, bundleId);
            filter.put(this.bundleValuesTextKeyColumn, bundleValues.get(i).getTextKey());

            Object key = this.getBundleValueKey(filter);
            if (key != null) {
                // delete
                filter.put(this.bundleValuesKeyColumn, key);
                this.daoBundleValues.delete(filter);
            }

        }

    }

    /**
     * Gets the bundle.
     * @param bundleDescriptor the bundle descriptor
     * @param locale the locale
     * @return the bundle
     */
    protected ResourceBundle getBundle(DatabaseBundleDescriptor bundleDescriptor, Locale locale) {
        String localeColumn = this.getColumnName(locale);
        if (localeColumn != null) {
            EntityResult resBundleValues = this.queryBundleValues(bundleDescriptor.getBundleId(), localeColumn);
            List<?> keys = (List<?>) resBundleValues.get(this.bundleValuesTextKeyColumn);
            List<?> translations = (List<?>) resBundleValues.get(localeColumn);
            if ((keys != null) && (translations != null)) {
                Map<String, Object> data = new HashMap<>();
                for (int i = 0; i < keys.size(); i++) {
                    if ((keys.get(i) != null) && (translations.get(i) != null)) {
                        data.put((String) keys.get(i), (String) translations.get(i));
                    }
                }
                return new TmpDatabaseResourceBundle((EntityResultMapImpl) data, locale);
            }
        }
        return null;
    }

    /**
     * Query bundle values.
     * @param bundleId the bundle id
     * @param languageColumn the language column
     * @return the entity result
     */
    protected EntityResult queryBundleValues(Object bundleId, String languageColumn) {
        Map<String, Object> filter = new HashMap<>();
        if (bundleId != null) {
            filter.put(this.bundleKeyColumn, bundleId);
        } else {
            filter.put(this.bundleKeyColumn, new SearchValue(SearchValue.NULL, null));
        }
        return this.daoBundleValues.query(filter,
                Arrays.asList(new String[] { this.bundleValuesTextKeyColumn, languageColumn }), (List<String>) null,
                (String) null);
    }

    /**
     * Gets the column name.
     * @param locale the locale
     * @return the column name
     */
    protected String getColumnName(Locale locale) {
        String country = locale.getCountry();
        String language = locale.getLanguage();
        String key = language + "_" + country;
        String variant = locale.getVariant();
        if ((variant != null) && (variant.length() > 0)) {
            key = key + "_" + variant;
        }

        if (!this.localeColumnNames.containsKey(key)) {

            // If localeColumnNames is null then the names of the columns have to
            // be defined in the constructor using parameters
            if (this.toLowerCase != null) {
                if (this.toLowerCase.booleanValue()) {
                    this.localeColumnNames.remove(key.toLowerCase());
                    this.localeColumnNames.put(key, key.toLowerCase());
                } else {
                    this.localeColumnNames.remove(key.toUpperCase());
                    this.localeColumnNames.put(key, key.toUpperCase());
                }
            }
        }

        return this.localeColumnNames.get(key);
    }

    /**
     * Gets the bundle descriptor.
     * @param bundleClassName the bundle class name
     * @return the bundle descriptor
     */
    protected DatabaseBundleDescriptor getBundleDescriptor(String bundleClassName) {
        DatabaseBundleDescriptor[] availableBundles = this.getAvailableBundles();
        if (availableBundles != null) {
            for (int i = 0; i < availableBundles.length; i++) {
                if (availableBundles[i].getBundleClassName().equals(bundleClassName)) {
                    return availableBundles[i];
                }
            }
        }
        return null;
    }

    /**
     * Gets the locale string.
     * @param locale the locale
     * @return the locale string
     */
    protected String getLocaleString(String locale) {
        List<Object> tokensAt = ParseTools.getTokensAt(locale, "_");
        if (tokensAt.size() >= 2) {
            tokensAt.set(0, tokensAt.get(0).toString().toLowerCase());
            tokensAt.set(1, tokensAt.get(1).toString().toUpperCase());
            if (tokensAt.size() > 2) {
                tokensAt.set(2, tokensAt.get(2).toString().toLowerCase());
            }
            return ParseTools.ListToStringSeparateBy(tokensAt, "_");
        }
        return null;
    }

    /**
     * Gets the bundle value key.
     * @param filter the filter
     * @return the bundle value key
     */
    protected Object getBundleValueKey(Map<String, Object> filter) {
        EntityResult queryResult = this.daoBundleValues.query(filter,
                Arrays.asList(new String[] { this.bundleValuesKeyColumn }), (List<String>) null, (String) null);
        CheckingTools.failIf(queryResult.calculateRecordNumber() > 1, "Too many bundle values found in database");
        if (queryResult.calculateRecordNumber() > 0) {
            return queryResult.getRecordValues(0).get(this.bundleValuesKeyColumn);
        } else {
            return null;
        }
    }

    /**
     * Gets the bundle id.
     * @param bundleClassName the bundle class name
     * @param availablebundles the availablebundles
     * @return the bundle id
     */
    protected Object getBundleId(String bundleClassName, List<DatabaseBundleDescriptor> availablebundles) {
        if (availablebundles != null) {
            for (DatabaseBundleDescriptor availableBundle : availablebundles) {
                if (availableBundle.getBundleClassName().equals(bundleClassName)) {
                    return availableBundle.getBundleId();
                }
            }
        }
        // If the bundle does not exist then it is possible to insert a new bundle
        Object id = this.insertNewBundle(bundleClassName);
        if (availablebundles != null) {
            availablebundles.add(new DatabaseBundleDescriptor(id, bundleClassName, null));
        }
        return id;
    }

    /**
     * Insert new bundle.
     * @param bundleClassName the bundle class name
     * @return the object
     */
    protected Object insertNewBundle(String bundleClassName) {
        Map<String, Object> hValues = new HashMap<>();
        hValues.put(this.bundleClassNameColumn, bundleClassName);
        EntityResult res = this.daoBundles.insert(hValues);
        return res.get(this.bundleKeyColumn);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CheckingTools.failIfEmptyString(this.bundleClassNameColumn,
                "No i18n database property classNameColumn defined");
        CheckingTools.failIfEmptyString(this.bundleDescriptionColumn,
                "No i18n database property descriptionColumn defined");
        CheckingTools.failIfEmptyString(this.bundleKeyColumn, "No i18n database property keyColumn defined");
        CheckingTools.failIfEmptyString(this.bundleValuesKeyColumn,
                "No i18n database property valuesKeyColumn defined");
        CheckingTools.failIfEmptyString(this.bundleValuesTextKeyColumn,
                "No i18n database property valuesTextKeyColumn defined");
        CheckingTools.failIfNull(this.daoBundleValues, "No i18n database bundle for values defined");
        CheckingTools.failIfNull(this.daoBundles, "No i18n database bundle for bundles defined");
        this.autoconfigureLocaleColumns();
    }

    /**
     * Gets the bundle class name column.
     * @return the bundleClassNameColumn
     */
    public String getBundleClassNameColumn() {
        return this.bundleClassNameColumn;
    }

    /**
     * Sets the bundle class name column.
     * @param bundleClassNameColumn the bundleClassNameColumn to set
     */
    public void setBundleClassNameColumn(String bundleClassNameColumn) {
        this.bundleClassNameColumn = bundleClassNameColumn;
    }

    /**
     * Gets the bundle description column.
     * @return the bundleDescriptionColumn
     */
    public String getBundleDescriptionColumn() {
        return this.bundleDescriptionColumn;
    }

    /**
     * Sets the bundle description column.
     * @param bundleDescriptionColumn the bundleDescriptionColumn to set
     */
    public void setBundleDescriptionColumn(String bundleDescriptionColumn) {
        this.bundleDescriptionColumn = bundleDescriptionColumn;
    }

    /**
     * Gets the bundle key column.
     * @return the bundleKeyColumn
     */
    public String getBundleKeyColumn() {
        return this.bundleKeyColumn;
    }

    /**
     * Sets the bundle key column.
     * @param bundleKeyColumn the bundleKeyColumn to set
     */
    public void setBundleKeyColumn(String bundleKeyColumn) {
        this.bundleKeyColumn = bundleKeyColumn;
    }

    /**
     * Gets the bundle values key column.
     * @return the bundleValuesKeyColumn
     */
    public String getBundleValuesKeyColumn() {
        return this.bundleValuesKeyColumn;
    }

    /**
     * Sets the bundle values key column.
     * @param bundleValuesKeyColumn the bundleValuesKeyColumn to set
     */
    public void setBundleValuesKeyColumn(String bundleValuesKeyColumn) {
        this.bundleValuesKeyColumn = bundleValuesKeyColumn;
    }

    /**
     * Gets the bundle values text key column.
     * @return the bundleValuesTextKeyColumn
     */
    public String getBundleValuesTextKeyColumn() {
        return this.bundleValuesTextKeyColumn;
    }

    /**
     * Sets the bundle values text key column.
     * @param bundleValuesTextKeyColumn the bundleValuesTextKeyColumn to set
     */
    public void setBundleValuesTextKeyColumn(String bundleValuesTextKeyColumn) {
        this.bundleValuesTextKeyColumn = bundleValuesTextKeyColumn;
    }

    /**
     * Gets the dao bundle values.
     * @return the daoBundleValues
     */
    public IOntimizeDaoSupport getDaoBundleValues() {
        return this.daoBundleValues;
    }

    /**
     * Sets the dao bundle values.
     * @param daoBundleValues the daoBundleValues to set
     */
    public void setDaoBundleValues(IOntimizeDaoSupport daoBundleValues) {
        this.daoBundleValues = daoBundleValues;
    }

    /**
     * Gets the dao bundles.
     * @return the daoBundles
     */
    public IOntimizeDaoSupport getDaoBundles() {
        return this.daoBundles;
    }

    /**
     * Sets the dao bundles.
     * @param daoBundles the daoBundles to set
     */
    public void setDaoBundles(IOntimizeDaoSupport daoBundles) {
        this.daoBundles = daoBundles;
    }

    /**
     * Gets the to lower case.
     * @return the toLowerCase
     */
    public Boolean getToLowerCase() {
        return this.toLowerCase;
    }

    /**
     * Sets the to lower case.
     * @param toLowerCase the toLowerCase to set
     */
    public void setToLowerCase(Boolean toLowerCase) {
        this.toLowerCase = toLowerCase;
    }

    /**
     * Gets the locale column names.
     * @return the localeColumnNames
     */
    public Map<String, String> getLocaleColumnNames() {
        return this.localeColumnNames;
    }

    /**
     * Sets the locale column names.
     * @param localeColumnNames the localeColumnNames to set
     */
    public void setLocaleColumnNames(Map<String, String> localeColumnNames) {
        this.localeColumnNames = localeColumnNames;
    }

}
