package com.ontimize.jee.server.services.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.ontimize.dto.EntityResult;
import com.ontimize.gui.SearchValue;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

/**
 * The Class FileRemoteApplicationPreferencesEngine.
 */
public class DatabaseRemoteApplicationPreferencesEngine
        implements IRemoteApplicationPreferencesEngine, InitializingBean {

    /** The dao. */
    private IOntimizeDaoSupport dao;

    /** The user column name. */
    private String userColumnName;

    /** The preference name column name. */
    private String preferenceNameColumnName;

    /** The preference value column name. */
    private String preferenceValueColumnName;

    /** The query id. */
    private String queryId;

    /**
     * Constructs a new DatabaseApplicationPreferences.
     *
     */
    public DatabaseRemoteApplicationPreferencesEngine() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        CheckingTools.failIfNull(this.dao, "Dao not found");
        CheckingTools.failIfEmptyString(this.userColumnName, "userColumnName not found");
        CheckingTools.failIfEmptyString(this.preferenceNameColumnName, "preferenceNameColumnName not found");
        CheckingTools.failIfEmptyString(this.preferenceValueColumnName, "prefrenceValueColumnName not found");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * getRemotePreference(java.lang.String, java.lang.String)
     */
    @Override
    public String getPreference(String user, String preferenceName) {
        Map<String, Object> keysValues = new HashMap<>();
        keysValues.put(this.userColumnName, user != null ? user : new SearchValue(SearchValue.NULL, null));
        keysValues.put(this.preferenceNameColumnName, preferenceName);
        List<String> attributes = Arrays.asList(new String[] { this.preferenceValueColumnName });
        EntityResult res = this.dao.query(keysValues, attributes, null, this.queryId);

        if (res.calculateRecordNumber() > 0) {
            List<?> pref = (List<?>) res.get(this.preferenceValueColumnName);
            if (pref != null) {
                Object prefValue = pref.get(0);
                if (prefValue == null) {
                    prefValue = "";
                }
                return prefValue.toString();
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * getDefaultPreference(java.lang.String)
     */
    @Override
    public String getDefaultPreference(String preferenceName) throws OntimizeJEERuntimeException {
        return this.getPreference(null, preferenceName);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * setRemotePreference(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void setPreference(String user, String preferenceName, String value) {
        String remotePreference = this.getPreference(user, preferenceName);
        if (remotePreference != null) {
            // update
            Map<String, Object> keysValues = new HashMap<>();
            keysValues.put(this.userColumnName, user != null ? user : new SearchValue(SearchValue.NULL, null));
            keysValues.put(this.preferenceNameColumnName, preferenceName);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(this.preferenceValueColumnName, value);
            this.dao.update(attributes, keysValues);
        } else {
            // insert
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(this.userColumnName, user);
            attributes.put(this.preferenceNameColumnName, preferenceName);
            attributes.put(this.preferenceValueColumnName, value);
            this.dao.insert(attributes);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * setDefaultPreference(java.lang.String, java.lang.String)
     */
    @Override
    public void setDefaultPreference(String preferenceName, String value) throws OntimizeJEERuntimeException {
        this.setPreference(null, preferenceName, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * loadRemotePreferences()
     */
    @Override
    public void loadPreferences() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * saveRemotePreferences()
     */
    @Override
    public void savePreferences() {
        // do nothing
    }

    /**
     * Gets the dao.
     * @return the dao
     */
    public IOntimizeDaoSupport getDao() {
        return this.dao;
    }

    /**
     * Sets the dao.
     * @param dao the dao to set
     */
    public void setDao(IOntimizeDaoSupport dao) {
        this.dao = dao;
    }

    /**
     * Gets the user column name.
     * @return the userColumnName
     */
    public String getUserColumnName() {
        return this.userColumnName;
    }

    /**
     * Sets the user column name.
     * @param userColumnName the userColumnName to set
     */
    public void setUserColumnName(String userColumnName) {
        this.userColumnName = userColumnName;
    }

    /**
     * Gets the preference name column name.
     * @return the preferenceNameColumnName
     */
    public String getPreferenceNameColumnName() {
        return this.preferenceNameColumnName;
    }

    /**
     * Sets the preference name column name.
     * @param preferenceNameColumnName the preferenceNameColumnName to set
     */
    public void setPreferenceNameColumnName(String preferenceNameColumnName) {
        this.preferenceNameColumnName = preferenceNameColumnName;
    }

    /**
     * Gets the preference value column name.
     * @return the preferenceValueColumnName
     */
    public String getPreferenceValueColumnName() {
        return this.preferenceValueColumnName;
    }

    /**
     * Sets the preference value column name.
     * @param preferenceValueColumnName the preferenceValueColumnName to set
     */
    public void setPreferenceValueColumnName(String preferenceValueColumnName) {
        this.preferenceValueColumnName = preferenceValueColumnName;
    }

    /**
     * Gets the query id.
     * @return the queryId
     */
    public String getQueryId() {
        return this.queryId;
    }

    /**
     * Sets the query id.
     * @param queryId the queryId to set
     */
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

}
