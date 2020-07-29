package com.ontimize.jee.server.services.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.CheckingTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRemoteApplicationPreferencesEngine implements IRemoteApplicationPreferencesEngine, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(FileRemoteApplicationPreferencesEngine.class);

    /** The file path. */
    private File filePath;

    /**
     * Character used to separate the user name and the preference name in the preference key
     */
    protected char separator = '_';

    /**
     * Preferences store
     */
    protected Properties props = new Properties();

    /**
     * Constructs a new BasicRemoteApplication.
     */
    public FileRemoteApplicationPreferencesEngine() {
        super();
    }

    /**
     * Sets the file path.
     * @param filePath the file path
     */
    public void setFilePath(File filePath) {
        this.filePath = filePath;
        CheckingTools.failIfNull(this.filePath, "Properties file path not defined");
        if (!filePath.exists()) {
            filePath.getParentFile().mkdirs();
        }
        this.loadPreferences();
    }

    /**
     * Gets the file path.
     * @return the file path
     */
    public File getFilePath() {
        return this.filePath;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        CheckingTools.failIfNull(this.filePath, "Properties file path not defined");
    }


    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * getRemotePreference(java.lang.String, java.lang.String)
     */
    @Override
    public String getPreference(String user, String preferenceName) {
        String key = this.getKeyPreference(user, preferenceName);

        String prop = this.props.getProperty(key);
        if (prop == null) {
            FileRemoteApplicationPreferencesEngine.logger.debug("Preference " + preferenceName
                    + " not found for the user: " + user + ". Returns the default preference value");

            // Get the default value if it exists
            return this.props.getProperty(preferenceName);
        }
        return prop;
    }

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
        String key = this.getKeyPreference(user, preferenceName);
        if (value != null) {
            this.props.setProperty(key, value);
        } else {
            this.props.remove(key);
        }
    }


    /**
     * Gets the key used to search a preference
     * @param user Name of the user who searches the preference. This parameter can be null
     * @param name Preference name
     * @return Key that must be used to search the preference
     */
    private String getKeyPreference(String user, String name) {
        if ((user == null) || (user.length() == 0)) {
            return name;
        }
        StringBuilder sb = new StringBuilder(user);
        sb.append(this.separator);
        sb.append(name);
        return sb.toString();
    }

    @Override
    public void setDefaultPreference(String preferenceName, String value) throws OntimizeJEERuntimeException {
        this.setPreference(null, preferenceName, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * saveRemotePreferences()
     */
    @Override
    public void savePreferences() {
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(this.filePath);
            this.props.store(fOut, "Application Preferences " + new java.util.Date().toString());
        } catch (Exception e) {
            FileRemoteApplicationPreferencesEngine.logger.error("Error saving preferences", e);
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (Exception e) {
                    FileRemoteApplicationPreferencesEngine.logger.trace(null, e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#
     * loadRemotePreferences()
     */
    @Override
    public void loadPreferences() {
        if (this.filePath.exists()) {
            FileInputStream fIn = null;
            try {
                fIn = new FileInputStream(this.filePath);
                this.props.load(fIn);
            } catch (Exception e) {
                FileRemoteApplicationPreferencesEngine.logger.trace(null, e);
            } finally {
                if (fIn != null) {
                    try {
                        fIn.close();
                    } catch (Exception e) {
                        FileRemoteApplicationPreferencesEngine.logger.trace(null, e);
                    }
                }
            }
        }
    }

}
