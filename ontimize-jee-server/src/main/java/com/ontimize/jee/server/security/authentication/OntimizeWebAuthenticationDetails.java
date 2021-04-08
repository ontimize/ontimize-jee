package com.ontimize.jee.server.security.authentication;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class OntimizeWebAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 1L;

    private final String scheme;

    private final String host;

    private final int port;

    private final String contextPath;

    /**
     * @param request
     */
    public OntimizeWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.scheme = request.getScheme();
        this.host = request.getServerName();
        this.port = request.getServerPort();
        this.contextPath = request.getContextPath();
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
