package com.ontimize.jee.desktopclient.locator.security;

import java.net.ConnectException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ValueChangeListener;
import com.ontimize.gui.ValueEvent;
import com.ontimize.jee.common.exceptions.InvalidCredentialsException;
import com.ontimize.jee.common.tools.ObjectWrapper;

public class OntimizeLoginProviderOauth2Handler {

    private static final Logger logger = LoggerFactory.getLogger(OntimizeLoginProviderOauth2Handler.class);

    public void doOauth2Authentication(final OntimizeLoginProvider provider, CloseableHttpResponse response)
            throws InterruptedException, ConnectException, InvalidCredentialsException {
        String location = response.getHeaders("Location")[0].getValue();
        final LoginIntoOAuth2Dialog redirectDialog = new LoginIntoOAuth2Dialog();
        final ObjectWrapper<String> redirectUrl = new ObjectWrapper<>();
        redirectUrl.setListener(new ValueChangeListener() {

            @Override
            public void valueChanged(ValueEvent event) {
                redirectDialog.setVisible(false);
                synchronized (redirectUrl) {
                    redirectUrl.notify();
                }
            }
        });
        synchronized (redirectUrl) {
            redirectDialog.showLoginScreen(location, redirectUrl);
            redirectUrl.wait();
            OntimizeLoginProviderOauth2Handler.this.redirectForOAuth(provider, redirectUrl.getValue());
        }
    }

    private void redirectForOAuth(OntimizeLoginProvider provider, String redirectUrlWithToken)
            throws ConnectException, InvalidCredentialsException {
        provider.doLogin(redirectUrlWithToken);
    }

}
