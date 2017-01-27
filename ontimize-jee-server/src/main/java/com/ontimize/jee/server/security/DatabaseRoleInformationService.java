/**
 * PermissionsProvider.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.server.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.security.authorization.Role;
import com.ontimize.security.XMLClientUtilities;

/**
 * The Class PermissionsProvider.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class DatabaseRoleInformationService implements ISecurityRoleInformationService {

	public static final String	BEAN_NAME	= "databaseRoleInformationService";
	/** The Constant logger. */
	private static final Logger	logger	= LoggerFactory.getLogger(DatabaseRoleInformationService.class);

	/** The profile repository. */
	private IOntimizeDaoSupport	profileRepository;
	/** The role name column. */
	private String				roleNameColumn;
	/** The permission key column. */
	private String				serverPermissionKeyColumn;
	/** The client permission column. */
	private String				clientPermissionColumn;
	/** The query id. */
	private String				serverPermissionQueryId;

	/** The client permission query id. */
	private String				clientPermissionQueryId;

	/**
	 * Instantiates a new default security authorizator.
	 */
	public DatabaseRoleInformationService() {
		super();
	}

	/**
	 * Load roles from database.
	 */
	@Override
	public Role loadRole(String roleName) {
		if (roleName == null) {
			return null;
		}

		final Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(this.roleNameColumn, roleName);
		final List<String> columnsToQuery = Arrays.asList(new String[] { this.roleNameColumn, this.clientPermissionColumn });
		final EntityResult res = this.profileRepository.query(filter, columnsToQuery, null, this.clientPermissionQueryId);
		if (res.calculateRecordNumber() == 0) {
			return null;
		}
		CheckingTools.failIf(res.calculateRecordNumber() > 1, "Multiple results for role");
		final List<?> clientPermissions = (List<?>) res.get(this.clientPermissionColumn);
		Map<String, ?> clientPermission = new Hashtable<String, Object>();
		try {
			final String plainClientPermission = (String) clientPermissions.get(0);
			if (plainClientPermission != null) {
				clientPermission = XMLClientUtilities.buildClientPermissions(new StringBuffer(plainClientPermission));
			}
		} catch (final Exception ex) {
			DatabaseRoleInformationService.logger.error("Error loading client permissions for role {}", roleName, ex);
		}
		try {
			List<String> serverPermissions = this.loadServerPermissions(roleName);
			return new Role(roleName, serverPermissions, clientPermission);
		} catch (final Exception ex) {
			DatabaseRoleInformationService.logger.error("Error loading server permissions for role {}", roleName, ex);
			return null;
		}
	}

	/**
	 * Loaod server permissions.
	 *
	 * @param roleName
	 *            the role name
	 * @return the list
	 */
	protected List<String> loadServerPermissions(final String roleName) {
		final List<String> listPermission = new ArrayList<String>();
		final Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(this.roleNameColumn, roleName);
		final List<String> columnsToQuery = Arrays.asList(new String[] { this.roleNameColumn, this.serverPermissionKeyColumn });
		final EntityResult res = this.profileRepository.query(filter, columnsToQuery, null, this.serverPermissionQueryId);

		final int nregs = res.calculateRecordNumber();
		final List<?> roleNames = (List<?>) res.get(this.roleNameColumn);
		final List<?> permissionNames = (List<?>) res.get(this.serverPermissionKeyColumn);
		for (int i = 0; i < nregs; i++) {
			final String dbRoleName = (String) roleNames.get(i);
			if (roleName.equals(dbRoleName)) {
				listPermission.add((String) permissionNames.get(i));
			}
		}
		return listPermission;
	}

	/**
	 * @param profileRepository
	 *            the profileRepository to set
	 */
	public void setProfileRepository(IOntimizeDaoSupport profileRepository) {
		this.profileRepository = profileRepository;
	}

	/**
	 * @param roleNameColumn
	 *            the roleNameColumn to set
	 */
	public void setRoleNameColumn(String roleNameColumn) {
		this.roleNameColumn = roleNameColumn;
	}

	/**
	 * @param serverPermissionKeyColumn
	 *            the serverPermissionKeyColumn to set
	 */
	public void setServerPermissionKeyColumn(String serverPermissionKeyColumn) {
		this.serverPermissionKeyColumn = serverPermissionKeyColumn;
	}

	/**
	 * @param clientPermissionColumn
	 *            the clientPermissionColumn to set
	 */
	public void setClientPermissionColumn(String clientPermissionColumn) {
		this.clientPermissionColumn = clientPermissionColumn;
	}

	/**
	 * @param serverPermissionQueryId
	 *            the serverPermissionQueryId to set
	 */
	public void setServerPermissionQueryId(String serverPermissionQueryId) {
		this.serverPermissionQueryId = serverPermissionQueryId;
	}

	/**
	 * @param clientPermissionQueryId
	 *            the clientPermissionQueryId to set
	 */
	public void setClientPermissionQueryId(String clientPermissionQueryId) {
		this.clientPermissionQueryId = clientPermissionQueryId;
	}

}
