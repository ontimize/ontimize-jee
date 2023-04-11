/**
 *
 */
package com.ontimize.jee.server.security.authentication;

import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.server.configuration.OntimizeConfiguration;
import com.ontimize.jee.server.security.ISecurityUserInformationService;
import com.ontimize.jee.server.security.encrypt.IPasswordEncryptHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The Class OntimizeAuthenticationProvider.
 *
 * @author joaquin.romero
 */
public class OntimizeAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider
        implements ApplicationContextAware {

    public final static Object NO_AUTHENTICATION_TOKEN = new Object();

    private ISecurityUserInformationService userInformationService;

    @Autowired(required = false)
    private IPasswordEncryptHelper passwordEncrypter;

    /**
     * Instantiates a new ontimize authentication provider.
     */
    public OntimizeAuthenticationProvider() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider#
     * additionalAuthenticationChecks(org.springframework .security.core.userdetails.UserDetails,
     * org.springframework.security.authentication.UsernamePasswordAuthenticationToken)
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (OntimizeAuthenticationProvider.NO_AUTHENTICATION_TOKEN != authentication.getCredentials()) {
            Object pass = authentication.getCredentials();
            if (pass == null) {
                throw new AuthenticationCredentialsNotFoundException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);
            }
            checkPassAgainstStoredCred(userDetails, pass);
        }
    }

    private void checkPassAgainstStoredCred(UserDetails userDetails, Object pass) {
        if (this.passwordEncrypter != null) {
            this.passwordEncrypter.checkPasswords(userDetails.getPassword(), pass);
        } else {
            if ((pass == null) || (!userDetails.getPassword().equals(pass))) {
                throw new AuthenticationCredentialsNotFoundException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider#
     * retrieveUser(java.lang.String,
     * org.springframework.security.authentication.UsernamePasswordAuthenticationToken)
     */
    @Override
    protected UserDetails retrieveUser(String userLogin, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        UserDetails userDetails = this.getUserCache().getUserFromCache(userLogin);
        if (userDetails == null) {
            userDetails = this.userInformationService.loadUserByUsername(userLogin);
        }
        if (userDetails != null) {
            return userDetails;
        }
        throw new AuthenticationCredentialsNotFoundException(userLogin);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.userInformationService = applicationContext.getBean(OntimizeConfiguration.class)
                .getSecurityConfiguration()
                .getUserInformationService();
    }

    public void setUserInformationService(ISecurityUserInformationService userInformationService) {
        this.userInformationService = userInformationService;
    }

}
