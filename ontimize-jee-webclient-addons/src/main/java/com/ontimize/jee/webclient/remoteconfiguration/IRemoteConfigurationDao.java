package com.ontimize.jee.webclient.remoteconfiguration;

import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

/**
 * DAO interface for injection purposes
 */
public interface IRemoteConfigurationDao extends IOntimizeDaoSupport {

	public static final String	DEFAULT_COLUMN_USER		= "USER_";
	public static final String	DEFAULT_COLUMN_APP		= "APP_UUID";
	public static final String	DEFAULT_COLUMN_CONFIG	= "CONFIGURATION";
	
}
