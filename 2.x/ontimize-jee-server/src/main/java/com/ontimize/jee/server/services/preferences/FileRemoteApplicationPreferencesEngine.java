package com.ontimize.jee.server.services.preferences;

import java.io.File;

import org.springframework.beans.factory.InitializingBean;

import com.ontimize.gui.preferences.BasicApplicationPreferences;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.CheckingTools;

public class FileRemoteApplicationPreferencesEngine implements IRemoteApplicationPreferencesEngine, InitializingBean {

	/** The basic application preferences. */
	private BasicApplicationPreferences	basicApplicationPreferences	= null;

	/** The file path. */
	private File						filePath;

	/**
	 * Constructs a new BasicRemoteApplication.
	 */
	public FileRemoteApplicationPreferencesEngine() {
		super();
	}

	/**
	 * Sets the file path.
	 *
	 * @param filePath
	 *            the file path
	 */
	public void setFilePath(File filePath) {
		this.filePath = filePath;
		CheckingTools.failIfNull(this.filePath, "Properties file path not defined");
		if (!filePath.exists()) {
			filePath.getParentFile().mkdirs();
		}
		String fileName = filePath.getName();
		String folderPath = filePath.getParentFile().getAbsolutePath();
		this.basicApplicationPreferences = new BasicApplicationPreferences(fileName, folderPath, null);
		this.basicApplicationPreferences.setLoadDefaults(false);
		this.basicApplicationPreferences.loadPreferences();
	}

	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public File getFilePath() {
		return this.filePath;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		CheckingTools.failIfNull(this.filePath, "Properties file path not defined");
	}

	/**
	 * Gets the basic application preferences.
	 *
	 * @return the basic application preferences
	 */
	public BasicApplicationPreferences getBasicApplicationPreferences() {
		return this.basicApplicationPreferences;
	}

	/**
	 * Sets the basic application preferences.
	 *
	 * @param basicApplicationPreferences
	 *            the basic application preferences
	 */
	public void setBasicApplicationPreferences(BasicApplicationPreferences basicApplicationPreferences) {
		this.basicApplicationPreferences = basicApplicationPreferences;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#getRemotePreference(java.lang.String, java.lang.String)
	 */
	@Override
	public String getPreference(String user, String preferenceName) {
		return this.basicApplicationPreferences.getPreference(user, preferenceName);
	}

	@Override
	public String getDefaultPreference(String preferenceName) throws OntimizeJEERuntimeException {
		return this.getPreference(null, preferenceName);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#setRemotePreference(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setPreference(String user, String preferenceName, String value) {
		this.basicApplicationPreferences.setPreference(user, preferenceName, value);
	}

	@Override
	public void setDefaultPreference(String preferenceName, String value) throws OntimizeJEERuntimeException {
		this.setPreference(null, preferenceName, value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#saveRemotePreferences()
	 */
	@Override
	public void savePreferences() {
		this.basicApplicationPreferences.savePreferences();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ontimize.jee.server.services.preferences.IRemoteApplicationPreferencesEngine#loadRemotePreferences()
	 */
	@Override
	public void loadPreferences() {
		this.basicApplicationPreferences.loadPreferences();
	}
}
