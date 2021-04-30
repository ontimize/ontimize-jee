package com.ontimize.jee.server.security.authentication.basic;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.Assert;

import com.ontimize.jee.server.security.authentication.AuthenticationResult;
import com.ontimize.jee.server.security.authentication.IAuthenticationMechanism;

public class BasicAuthenticationMechanism implements IAuthenticationMechanism {

    private static final Logger logger = LoggerFactory.getLogger(BasicAuthenticationMechanism.class);

    private String credentialsCharset = "UTF-8";

    @Override
    public AuthenticationResult authenticate(HttpServletRequest request, HttpServletResponse response,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        String header = request.getHeader("Authorization");

        if ((header == null) || !header.startsWith("Basic ")) {
            return null;
        }

        String[] tokens = this.extractAndDecodeHeader(header, request);
        assert tokens.length == 2;

        String username = tokens[0];

        BasicAuthenticationMechanism.logger.debug("Basic Authentication Authorization header found for user '{}'",
                username);

        return new AuthenticationResult(true, new UsernamePasswordAuthenticationToken(username, tokens[1]));
    }

    /**
     * Decodes the header into a username and password.
     * @throws BadCredentialsException if the Basic header is not present or is not valid Base64
     */
    private String[] extractAndDecodeHeader(String header, HttpServletRequest request) {

        try {
            byte[] base64Token = header.substring(6).getBytes("UTF-8");
            byte[] decoded;
            decoded = Base64.decode(base64Token);

            String token = new String(decoded, this.getCredentialsCharset(request));

            int delim = token.indexOf(":");

            if (delim == -1) {
                throw new BadCredentialsException("Invalid basic authentication token");
            }
            return new String[] { token.substring(0, delim), token.substring(delim + 1) };
        } catch (IllegalArgumentException | UnsupportedEncodingException error) {
            throw new BadCredentialsException("Failed to decode basic authentication token", error);
        }
    }

    public void setCredentialsCharset(String credentialsCharset) {
        Assert.hasText(credentialsCharset, "credentialsCharset cannot be null or empty");
        this.credentialsCharset = credentialsCharset;
    }

    protected String getCredentialsCharset(HttpServletRequest httpRequest) {
        return this.credentialsCharset;
    }

}
