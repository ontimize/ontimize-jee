package com.ontimize.jee.server.security.authentication.oauth2;

import java.util.Map;

/**
 * @author
 * @param <U>
 * @param <I>
 */
public interface IOAuth2ClientLoginListener {

    void onNewOauth2SuccesfullLogin(String token, String userIdAsString, Map<String, Object> oauth2UserInfo);

}
