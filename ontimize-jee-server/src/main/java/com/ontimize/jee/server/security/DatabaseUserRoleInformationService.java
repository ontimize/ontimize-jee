package com.ontimize.jee.server.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ontimize.db.EntityResult;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

/**
 * The Class PermissionsProvider.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class DatabaseUserRoleInformationService implements ISecurityUserRoleInformationService {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUserRoleInformationService.class);

    public static final String BEAN_NAME = "databaseUserRoleInformationService";

    /** The user roles repository. */
    private IOntimizeDaoSupport userRolesRepository;

    /** The role login column. */
    private String roleLoginColumn;

    /** The role name column. */
    private String roleNameColumn;

    /** The role query id. */
    private String roleQueryId;

    /**
     * Instantiates a new default security authorizator.
     */
    public DatabaseUserRoleInformationService() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.server.security.ISecurityUserRoleInformationService#loadUserRoles(java.lang.
     * String)
     */
    @Override
    public Collection<GrantedAuthority> loadUserRoles(String userLogin) {
        DatabaseUserRoleInformationService.logger.trace(String.format("Getting user details for %s", userLogin));
        Map<String, Object> filter = new HashMap<>();
        filter.put(this.roleLoginColumn, userLogin);
        List<String> columnsToQuery = Arrays.asList(new String[] { this.roleNameColumn });
        EntityResult resRoles = this.userRolesRepository.query(filter, columnsToQuery, null, this.roleQueryId);
        List<Object> roles = (List<Object>) resRoles.get(this.roleNameColumn);
        List<GrantedAuthority> authorities = new ArrayList<>();
        if ((roles != null) && (roles.size() > 0)) {
            for (Object ob : roles) {
                authorities.add(new SimpleGrantedAuthority((String) ob));
            }
        }
        return authorities;
    }

    /**
     * @param userRolesRepository the userRolesRepository to set
     */
    public void setUserRolesRepository(IOntimizeDaoSupport userRolesRepository) {
        this.userRolesRepository = userRolesRepository;
    }

    /**
     * @param roleLoginColumn the roleLoginColumn to set
     */
    public void setRoleLoginColumn(String roleLoginColumn) {
        this.roleLoginColumn = roleLoginColumn;
    }

    /**
     * @param roleNameColumn the roleNameColumn to set
     */
    public void setRoleNameColumn(String roleNameColumn) {
        this.roleNameColumn = roleNameColumn;
    }

    /**
     * @param roleQueryId the roleQueryId to set
     */
    public void setRoleQueryId(String roleQueryId) {
        this.roleQueryId = roleQueryId;
    }

}
