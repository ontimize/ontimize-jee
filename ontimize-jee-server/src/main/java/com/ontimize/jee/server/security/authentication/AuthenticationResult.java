package com.ontimize.jee.server.security.authentication;

import org.springframework.security.core.Authentication;

/**
 * The Class AuthenticationResult.
 */
public class AuthenticationResult {

    /** The authentication. */
    private final Authentication authentication;

    /** The generate jw token. */
    private final boolean generateJWToken;

    /**
     * Instantiates a new authentication result.
     * @param returnNullOnSuccess the return null on success
     * @param generateJWToken the generate jw token
     * @param authentication the authentication
     */
    public AuthenticationResult(boolean generateJWToken, Authentication authentication) {
        super();
        this.authentication = authentication;
        this.generateJWToken = generateJWToken;
    }

    /**
     * Gets the authentication.
     * @return the authentication
     */
    public Authentication getAuthentication() {
        return this.authentication;
    }

    /**
     * Checks if is generate jw token.
     * @return true, if is generate jw token
     */
    public boolean isGenerateJWToken() {
        return this.generateJWToken;
    }

}
