package com.ontimize.jee.server.security.authentication.oauth2;
import java.util.Map;

/**
 * @author
 */
public interface IOAuth2ClientUserInfoProvider {

	/**
	 * @param token
	 * @return
	 */
	Map<String, Object> getUserInfoFromProvider(String token, OAuth2ClientProperties oAuth2ClientProperties);
}