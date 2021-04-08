package com.ontimize.jee.server.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.security.authorization.ISecurityAuthorizator;
import com.ontimize.security.XMLClientUtilities;

/**
 * The Class PermissionsProvider.
 *
 * @author <a href="user@email.com">Author</a>
 */
public class DatabaseUserInformationService implements ISecurityUserInformationService, ApplicationContextAware {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUserInformationService.class);

    /** The user repository. */
    private IOntimizeDaoSupport userRepository;

    /** The user login column. */
    private String userLoginColumn;

    /** The user password column. */
    private String userPasswordColumn;

    /** The user other data columns. */
    private List<String> userOtherDataColumns;

    /** The user need check pass column. */
    private String userNeedCheckPassColumn;

    /** The user query id. */
    private String userQueryId;

    private ISecurityUserRoleInformationService userRoleInformationService;

    /** The application context. */
    private ApplicationContext applicationContext;

    /**
     * Instantiates a new default security authorizator.
     */
    public DatabaseUserInformationService() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.server.security.authentication.ISecurityAuthenticator#getUserDetails(java.lang.
     * String, org.springframework.security.authentication.AbstractAuthenticationToken)
     */
    @Override
    public UserInformation loadUserByUsername(String userLogin) {
        if (userLogin == null) {
            throw new UsernameNotFoundException("username was null");
        }

        // Obtenemos datos del usuario
        Map<?, ?> userInfo = this.queryUserInformation(userLogin);
        if (userInfo == null) {
            throw new UsernameNotFoundException(String.format("user %s not found", userLogin));
        }
        // Obtenemos roles del usuario
        Collection<GrantedAuthority> authorities = this.getUserRoleInformationService() == null ? null
                : this.getUserRoleInformationService().loadUserRoles(userLogin);
        Map<String, ?> clientPermissions = this.getClientPermissions(authorities);

        UserInformation userInformation = new UserInformation(userLogin, (String) userInfo.get(this.userPasswordColumn),
                authorities, clientPermissions);
        if (this.userOtherDataColumns != null) {
            for (String key : this.userOtherDataColumns) {
                userInformation.addOtherData(key, userInfo.get(key));
            }
        }
        return userInformation;
    }

    /**
     * Query user information.
     * @param userLogin the user login
     * @param authentication the authentication
     * @return the map
     */
    protected Map<?, ?> queryUserInformation(String userLogin) {
        Map<String, Object> filter = new HashMap<>();
        filter.put(this.userLoginColumn, userLogin);
        List<String> columnsToQuery = new ArrayList<>();
        columnsToQuery.add(this.userLoginColumn);
        columnsToQuery.add(this.userPasswordColumn);
        if (this.userNeedCheckPassColumn != null) {
            columnsToQuery.add(this.userNeedCheckPassColumn);
        }
        if (this.userOtherDataColumns != null) {
            columnsToQuery.addAll(this.userOtherDataColumns);
        }
        EntityResult resUser = this.userRepository.query(filter, columnsToQuery, null, this.userQueryId);
        if (resUser.calculateRecordNumber() != 1) {
            return null;
        }
        return resUser.getRecordValues(0);
    }

    @Override
    public List<UserInformation> getAllUserInformation() throws OntimizeJEEException {
        List<UserInformation> toret = new ArrayList<>();
        List<String> listLogin = this.getAllUserInformationLogin();
        for (int i = 0; i < listLogin.size(); i++) {
            String login = listLogin.get(i);
            toret.add(this.loadUserByUsername(login));
        }
        return toret;
    }

    @Override
    public List<String> getAllUserInformationLogin() throws OntimizeJEEException {
        List<String> toret = new ArrayList<>();
        List<String> columnsToQuery = new ArrayList<>();
        columnsToQuery.add(this.userLoginColumn);
        EntityResult resUser = this.userRepository.query(new HashMap(), columnsToQuery, null, this.userQueryId);
        for (int i = 0; i < resUser.calculateRecordNumber(); i++) {
            String login = (String) resUser.getRecordValues(i).get(this.userLoginColumn);
            toret.add(login);
        }
        return toret;
    }

    /**
     * Gets the client permissions.
     * @param authorities the authorities
     * @return the client permissions
     */
    protected Map<String, ?> getClientPermissions(Collection<GrantedAuthority> authorities) {
        if (authorities == null) {
            return null;
        }
        OntimizeConfiguration ontimizeConfiguration = this.applicationContext.getBean(OntimizeConfiguration.class);
        ISecurityAuthorizator authorizator = ontimizeConfiguration.getSecurityConfiguration().getAuthorizator();

        List<Map<String, ?>> clientPermissions = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            Map<String, ?> roleClientPermission = authorizator.getRole(authority.getAuthority()).getClientPermissions();
            if (roleClientPermission != null) {
                clientPermissions.add(roleClientPermission);
            }
        }
        try {
            return XMLClientUtilities.joinClientPermissions(clientPermissions);
        } catch (Exception e) {
            DatabaseUserInformationService.logger.error("Error joining clientPermissions for authorities {}",
                    authorities);
            throw new OntimizeJEERuntimeException("Error joining clientPermissions", e);
        }
    }

    public ISecurityUserRoleInformationService getUserRoleInformationService() {
        if (this.userRoleInformationService == null) {
            this.userRoleInformationService = this.applicationContext.getBean(OntimizeConfiguration.class)
                .getSecurityConfiguration()
                .getUserRoleInformationService();
        }
        return this.userRoleInformationService;
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
        this.applicationContext = applicationContext;
    }

    /**
     * @param userRepository the userRepository to set
     */
    public void setUserRepository(IOntimizeDaoSupport userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param userLoginColumn the userLoginColumn to set
     */
    public void setUserLoginColumn(String userLoginColumn) {
        this.userLoginColumn = userLoginColumn;
    }

    /**
     * @param userPasswordColumn the userPasswordColumn to set
     */
    public void setUserPasswordColumn(String userPasswordColumn) {
        this.userPasswordColumn = userPasswordColumn;
    }

    /**
     * @param userOtherDataColumns the userOtherDataColumns to set
     */
    public void setUserOtherDataColumns(String userOtherDataColumns) {
        this.userOtherDataColumns = userOtherDataColumns == null ? null
                : Arrays.asList(userOtherDataColumns.split(";"));
    }

    /**
     * @param userQueryId the userQueryId to set
     */
    public void setUserQueryId(String userQueryId) {
        this.userQueryId = userQueryId;
    }

    public void setUserNeedCheckPassColumn(String userNeedCheckPassColumn) {
        this.userNeedCheckPassColumn = userNeedCheckPassColumn;
    }

}
