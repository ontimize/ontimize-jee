package com.ontimize.jee.server.security.authentication.jwt;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class DefaultJwtTokenService.
 */
public class DefaultJwtService implements IJwtService {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DefaultJwtService.class);

    /** The object mapper. */
    private ObjectMapper objectMapper = new ObjectMapper();

    /** The secret. */
    private String secret;

    /** The signer verifier. */
    private final SignerVerifier signerVerifier;

    /**
     * Instantiates a new default jwt token service.
     * @param secret the secret
     */
    public DefaultJwtService(String secret) {
        Assert.notNull(secret, "secret must not be null");
        this.secret = secret;
        this.signerVerifier = new MacSigner(secret);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.security.authentication.jwt.IJwtTokenService#sign(java.util.Map)
     */
    @Override
    public String sign(Map<String, Object> claims) {
        try {
            Jwt jwt = JwtHelper.encode(this.objectMapper.writeValueAsString(claims), this.signerVerifier);
            return jwt.getEncoded();
        } catch (JsonProcessingException error) {
            DefaultJwtService.logger.error(null, error);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ontimize.jee.server.security.authentication.jwt.IJwtTokenService#verify(java.lang.String)
     */
    @Override
    public Map<String, Object> verify(String token) {
        Jwt jwt = JwtHelper.decodeAndVerify(token, this.signerVerifier);
        try {
            return this.objectMapper.readValue(jwt.getClaims(), Map.class);
        } catch (IOException error) {
            DefaultJwtService.logger.error(null, error);
            return null;
        }
    }

    /**
     * Gets the object mapper.
     * @return the object mapper
     */
    protected ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * Sets the object mapper.
     * @param objectMapper the new object mapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Gets the secret.
     * @return the secret
     */
    public String getSecret() {
        return this.secret;
    }

    /**
     * Sets the secret.
     * @param secret the new secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

}
