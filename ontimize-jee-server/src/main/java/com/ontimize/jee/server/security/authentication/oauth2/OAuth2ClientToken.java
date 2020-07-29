package com.ontimize.jee.server.security.authentication.oauth2;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class OAuth2ClientToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private Serializable credential = null;

    private Serializable principal = null;

    private String redirectUri = null;

    /**
     * @param credential
     */
    public OAuth2ClientToken(Serializable credential) {
        super(null);
        this.credential = credential;
        this.setAuthenticated(false);
    }

    /**
     * @param principal
     * @param credential
     * @param authorities
     */
    public OAuth2ClientToken(Serializable principal, Serializable credential,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.credential = credential;
        this.principal = principal;

        this.setAuthenticated(true);
    }

    /**
     * @return
     */
    @Override
    public Object getCredentials() {
        return this.credential;
    }

    /**
     * @return
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * @return
     */
    public String getRedirectUri() {
        return this.redirectUri;
    }

    /**
     * @param redirectUri
     */
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OAuth2ClientToken)) {
            return false;
        }

        OAuth2ClientToken test = (OAuth2ClientToken) obj;

        if (!this.getAuthorities().equals(test.getAuthorities())) {
            return false;
        }

        if ((this.getDetails() == null) && (test.getDetails() != null)) {
            return false;
        }

        if ((this.getDetails() != null) && (test.getDetails() == null)) {
            return false;
        }

        if ((this.getDetails() != null) && !this.getDetails().equals(test.getDetails())) {
            return false;
        }

        if ((this.getCredentials() == null) && (test.getCredentials() != null)) {
            return false;
        }

        if ((this.getCredentials() != null) && !this.getCredentials().equals(test.getCredentials())) {
            return false;
        }

        if ((this.getPrincipal() == null) && (test.getPrincipal() != null)) {
            return false;
        }

        if ((this.getPrincipal() != null) && !this.getPrincipal().equals(test.getPrincipal())) {
            return false;
        }

        return this.isAuthenticated() == test.isAuthenticated();
    }

    @Override
    public int hashCode() {
        int code = 31;

        for (GrantedAuthority authority : this.getAuthorities()) {
            code ^= authority.hashCode();
        }

        if (this.getPrincipal() != null) {
            code ^= this.getPrincipal().hashCode();
        }

        if (this.getCredentials() != null) {
            code ^= this.getCredentials().hashCode();
        }

        if (this.getDetails() != null) {
            code ^= this.getDetails().hashCode();
        }

        if (this.isAuthenticated()) {
            code ^= -37;
        }

        return code;
    }

}
