package com.ontimize.jee.common.services.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Server user information.
 *
 * @author <a href=""></a>
 */
public class UserInformation implements UserDetails, CredentialsContainer {

	/** The Constant serialVersionUID. */
	private static final long					serialVersionUID	= 1L;

	/** The other data. */
	private final Map<Object, Object>			otherData;
	/** The password. */
	private transient String					password;
	/** The username. */
	private final String						username;
	/** The authorities. */
	private final Collection<GrantedAuthority>	authorities;
	/** The account non expired. */
	private final boolean						accountNonExpired;
	/** The account non locked. */
	private final boolean						accountNonLocked;
	/** The credentials non expired. */
	private final boolean						credentialsNonExpired;
	/** The enabled. */
	private final boolean						enabled;
	/** The client permissions. */
	private final Map<String, ?>				clientPermissions;

	/**
	 * Calls the more complex constructor with all boolean arguments set to {@code true}.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param authorities
	 *            the authorities
	 * @param clientPermissions
	 *            the client permissions
	 */
	public UserInformation(String username, String password, Collection<? extends GrantedAuthority> authorities, Map<String, ?> clientPermissions) {
		this(username, password, true, true, true, true, authorities, clientPermissions);
	}

	/**
	 * Construct the <code>User</code> with the details required by {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.
	 *
	 * @param username
	 *            the username presented to the <code>DaoAuthenticationProvider</code>
	 * @param password
	 *            the password that should be presented to the <code>DaoAuthenticationProvider</code>
	 * @param enabled
	 *            set to <code>true</code> if the user is enabled
	 * @param accountNonExpired
	 *            set to <code>true</code> if the account has not expired
	 * @param credentialsNonExpired
	 *            set to <code>true</code> if the credentials have not expired
	 * @param accountNonLocked
	 *            set to <code>true</code> if the account is not locked
	 * @param authorities
	 *            the authorities that should be granted to the caller if they presented the correct username and password and the user is enabled. Not null.
	 * @param clientPermissions
	 *            the client permissions
	 */
	public UserInformation(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities, Map<String, ?> clientPermissions) {
		super();
		if (((username == null) || "".equals(username))) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}

		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.accountNonExpired = accountNonExpired;
		this.credentialsNonExpired = credentialsNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.authorities = Collections.unmodifiableCollection(authorities);
		this.otherData = new HashMap<>();
		this.clientPermissions = clientPermissions;

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.User#getPassword()
	 */
	@Override
	public String getPassword() {
		return this.password;
	}

	/**
	 * Adds the other data.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void addOtherData(Object key, Object value) {
		this.otherData.put(key, value);
	}

	/**
	 * Removes the other data.
	 *
	 * @param key
	 *            the key
	 * @return the object
	 */
	public Object removeOtherData(Object key) {
		return this.otherData.remove(key);
	}

	/**
	 * Gets the other data.
	 *
	 * @return the other data
	 */
	public Map<Object, Object> getOtherData() {
		return this.otherData;
	}

	/**
	 * Gets the login.
	 *
	 * @return the login
	 */
	public String getLogin() {
		return this.getUsername();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
	 */
	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
	 */
	@Override
	public String getUsername() {
		return this.username;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
	 */
	@Override
	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
	 */
	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.CredentialsContainer#eraseCredentials()
	 */
	@Override
	public void eraseCredentials() {
		// do nothing
	}

	/**
	 * Returns {@code true} if the supplied object is a {@code User} instance with the same {@code username} value. <p> In other words, the objects are equal if they have the same
	 * username, representing the same principal.
	 *
	 * @param rhs
	 *            the rhs
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object rhs) {
		if (rhs instanceof UserInformation) {
			return this.username.equals(((UserInformation) rhs).username);
		}
		return false;
	}

	/**
	 * Returns the hashcode of the {@code username}.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return this.username.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(": ");
		sb.append("Username: ").append(this.username).append("; ");
		sb.append("Password: [PROTECTED]; ");
		sb.append("Enabled: ").append(this.enabled).append("; ");
		sb.append("AccountNonExpired: ").append(this.accountNonExpired).append("; ");
		sb.append("credentialsNonExpired: ").append(this.credentialsNonExpired).append("; ");
		sb.append("AccountNonLocked: ").append(this.accountNonLocked).append("; ");

		if (!this.authorities.isEmpty()) {
			sb.append("Granted Authorities: ");

			boolean first = true;
			for (GrantedAuthority auth : this.authorities) {
				if (!first) {
					sb.append(",");
				}
				first = false;

				sb.append(auth);
			}
		} else {
			sb.append("Not granted any authorities");
		}

		return sb.toString();
	}

	/**
	 * Gets the client permissions.
	 *
	 * @return the client permissions
	 */
	public Map<String, ?> getClientPermissions() {
		return this.clientPermissions;
	}

	/**
	 * Allow to set password value.
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
