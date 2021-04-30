package com.ontimize.jee.desktopclient.locator.security;

import java.net.ConnectException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;

import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.hessian.OntimizeHessianHttpClientSessionProcessorFactory;
import com.ontimize.jee.common.hessian.OntimizeHessianProxyFactoryBean;
import com.ontimize.jee.common.security.ILoginProvider;
import com.ontimize.jee.common.services.user.IUserInformationService;
import com.ontimize.jee.common.tools.ObjectTools;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

public class OntimizeLoginProvider implements ILoginProvider {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeLoginProvider.class);

    private String lastUsedUser;

    private String lastUsedPassword;

    public OntimizeLoginProvider() {
        super();
    }

    @Override
    public synchronized void doLogin(URI baseUri, String user, String password)
            throws InvalidCredentialsException, ConnectException {
        if ((user != null) && (password != null)) {
            OntimizeHessianHttpClientSessionProcessorFactory.addCredentials(baseUri, user, password);
        }
        this.lastUsedUser = user;
        this.lastUsedPassword = password;
        try {
            String serviceUrl = this.getUserServiceUrl();
            this.doLogin(serviceUrl);
        } catch (InvalidCredentialsException | ConnectException ex) {
            throw ex;
        } catch (Exception ex) {
            OntimizeLoginProvider.logger.error(null, ex);
            throw new InvalidCredentialsException(ex);
        } finally {
            if ((user != null) && (password != null)) {
                OntimizeHessianHttpClientSessionProcessorFactory.removeCredentials(baseUri);
            }
        }
    }

    protected synchronized void doLogin(String serviceUrl) throws InvalidCredentialsException, ConnectException {
        OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN = null;
        try (CloseableHttpClient httpClient = OntimizeHessianHttpClientSessionProcessorFactory.createClient(-1)) {
            HttpGet request = new HttpGet(serviceUrl);
            CloseableHttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 401) {
                throw new InvalidCredentialsException(response.getStatusLine().getReasonPhrase());
            } else if (response.getStatusLine().getStatusCode() == 302) {
                Header[] authenticateHeader = response.getHeaders("WWW-Authenticate");
                if ((authenticateHeader != null) && (authenticateHeader.length > 0)
                        && "Bearer realm=\"oauth\"".equals(authenticateHeader[0].getValue())) {
                    new OntimizeLoginProviderOauth2Handler().doOauth2Authentication(this, response);
                }
            } else if (!ObjectTools.isIn(response.getStatusLine().getStatusCode(), 200, 405)) {
                throw new ConnectException(serviceUrl + ": " + response.getStatusLine().getReasonPhrase());
            }
        } catch (InvalidCredentialsException | ConnectException ex) {
            OntimizeLoginProvider.logger.error(null, ex);
            throw ex;
        } catch (Exception ex) {
            OntimizeLoginProvider.logger.error(null, ex);
            throw new InvalidCredentialsException("E_CONNECT_TO_SERVER", ex);
        }
    }

    private String getUserServiceUrl() {
        IUserInformationService userService = BeansFactory.getBean(IUserInformationService.class);

        // no pedemos autenticar con un servicio hessian porque la entity (inputstream) no es "repeatable" y
        // no puede llevar a cabo la negociacion
        String serviceUrl = ((OntimizeHessianProxyFactoryBean) ((Advised) userService).getAdvisors()[0].getAdvice())
            .getServiceUrl();
        return serviceUrl;
    }

    @Override
    public synchronized void doLogin(URI uri) throws InvalidCredentialsException, ConnectException {
        this.doLogin(uri, this.lastUsedUser, this.lastUsedPassword);
    }

}
