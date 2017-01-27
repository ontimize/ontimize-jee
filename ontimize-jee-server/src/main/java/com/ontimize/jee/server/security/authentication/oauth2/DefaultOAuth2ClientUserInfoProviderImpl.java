package com.ontimize.jee.server.security.authentication.oauth2;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.ontimize.jee.common.exceptions.InvalidCredentialsException;

public class DefaultOAuth2ClientUserInfoProviderImpl implements IOAuth2ClientUserInfoProvider {

	private final Logger			log	= LoggerFactory.getLogger(DefaultOAuth2ClientUserInfoProviderImpl.class);

	@Override
	public Map<String, Object> getUserInfoFromProvider(String token, OAuth2ClientProperties oAuth2ClientProperties) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
		ResponseEntity<Map> userInfoMapEntity = restTemplate.exchange(oAuth2ClientProperties.getUserInfoUri(), HttpMethod.GET, entity, Map.class);
		if (userInfoMapEntity.getStatusCode() == HttpStatus.OK) {
			return userInfoMapEntity.getBody();
		} else {
			this.log.error("cannot get user info");
			throw new InvalidCredentialsException(userInfoMapEntity.getStatusCode().getReasonPhrase());
		}
	}
}