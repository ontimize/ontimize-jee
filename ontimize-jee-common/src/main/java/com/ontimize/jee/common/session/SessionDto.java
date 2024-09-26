package com.ontimize.jee.common.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.websocket.Session;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;

import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.common.tools.MapTools;

public class SessionDto implements Serializable {

    private String id;

    private Map<String, Object> sessionAttrs = new HashMap<>();

    private long creationTime;

    private long lastAccessedTime;

    /**
     * Defaults to 30 minutes
     */
    private int maxInactiveInterval;

    /**
     * Creates a new instance
     */
    public SessionDto() {
    }

    /**
     * Creates a new instance from the provided {@link Session}
     * @param session the {@link Session} to initialize this {@link Session} with. Cannot be null.
     */
    public SessionDto(String id, HashMap<String, Object> sessionAttrs, long lastAccessedTime, long creationTime,
            int maxInactiveIntervalInSeconds) {
        this.id = id;
        this.sessionAttrs = sessionAttrs;
        this.lastAccessedTime = lastAccessedTime;
        this.creationTime = creationTime;
        this.maxInactiveInterval = maxInactiveIntervalInSeconds;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return this.id;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public void setMaxInactiveIntervalInSeconds(int interval) {
        this.maxInactiveInterval = interval;
    }

    public int getMaxInactiveIntervalInSeconds() {
        return this.maxInactiveInterval;
    }

    public boolean isExpired() {
        return this.isExpired(System.currentTimeMillis());
    }

    boolean isExpired(long now) {
        if (this.maxInactiveInterval < 0) {
            return false;
        }
        return (now - TimeUnit.SECONDS.toMillis(this.maxInactiveInterval)) >= this.lastAccessedTime;
    }

    public Object getAttribute(String attributeName) {
        return this.sessionAttrs.get(attributeName);
    }

    public Set<String> getAttributeNames() {
        return this.sessionAttrs.keySet();
    }

    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributeValue == null) {
            this.removeAttribute(attributeName);
        } else {
            this.sessionAttrs.put(attributeName, attributeValue);
        }
    }

    public void removeAttribute(String attributeName) {
        this.sessionAttrs.remove(attributeName);
    }

    /**
     * Sets the time that this {@link Session} was created in milliseconds since midnight of 1/1/1970
     * GMT. The default is when the {@link Session} was instantiated.
     * @param creationTime the time that this {@link Session} was created in milliseconds since midnight
     *        of 1/1/1970 GMT.
     */
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Sets the identifier for this {@link Session}. The id should be a secure random generated value to
     * prevent malicious users from guessing this value. The default is a secure random generated
     * identifier.
     * @param id the identifier for this session.
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SessionDto) && this.id.equals(((SessionDto) obj).getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ID: %s\tcreationTime: %d\tlastAccessTime: %d\tmaxInactiveIntervalInSeconds\n", this.id,
                this.creationTime, this.lastAccessedTime,
                this.getMaxInactiveIntervalInSeconds()));
        List<String> keySet = new ArrayList<>(this.sessionAttrs.keySet());
        if (keySet.remove("SPRING_SECURITY_CONTEXT")) {
            SecurityContextImpl context = (SecurityContextImpl) this.sessionAttrs.get("SPRING_SECURITY_CONTEXT");
            Authentication authentication = context.getAuthentication();
            UserInformation userInformation = (UserInformation) authentication.getPrincipal();
            sb.append("\t")
                .append(userInformation.getLogin())
                .append(", roles:")
                .append(userInformation.getAuthorities())
                .append("\n");
            sb.append("\tOther data:\n");
            MapTools.toString(userInformation.getOtherData(), "\t");
            sb.append("\tAuthentication details:\n");
            MapTools.toString((Map<?, ?>) authentication.getDetails(), "\t");

        }
        for (String key : keySet) {
            Object value = this.sessionAttrs.get(key);
            if (value instanceof Map) {
                sb.append("\t").append(key).append(":\n");
                sb.append(MapTools.toString((Map<Object, Object>) value, "\t\t"));
            } else {
                sb.append("\t").append(key).append(": ").append(value);
            }

        }
        return sb.toString();
    }

    private static final long serialVersionUID = 7160779239673823561L;

}
