package com.ontimize.jee.server.dao.jdbc.extension;

import com.ontimize.jee.server.dao.jdbc.setup.JdbcEntitySetupType;

/**
 * The implementations of this interface will manage Dao extensions
 */
public interface IDaoExtensionHelper {

	/**
	 * Look for extension files, and load it
	 *
	 * @param ontimizeJdbcDaoSupport
	 *
	 * @param path
	 * @param pathToPlaceHolder
	 */
	JdbcEntitySetupType checkDaoExtensions(JdbcEntitySetupType baseSetup, String path, String pathToPlaceHolder);

}
