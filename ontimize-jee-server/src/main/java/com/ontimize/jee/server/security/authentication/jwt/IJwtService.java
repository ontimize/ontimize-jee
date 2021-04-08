package com.ontimize.jee.server.security.authentication.jwt;

import java.util.Map;

/**
 * The Interface IJwtTokenService.
 */
public interface IJwtService {

    /**
     * Sign.
     * @param claims the claims
     * @return the string
     */
    String sign(Map<String, Object> claims);

    /**
     * Verify.
     * @param token the token
     * @return the map
     */
    Map<String, Object> verify(String token);

}
