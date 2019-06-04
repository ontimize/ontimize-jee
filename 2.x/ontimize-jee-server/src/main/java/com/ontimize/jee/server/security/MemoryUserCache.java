package com.ontimize.jee.server.security;

import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;

import com.ontimize.jee.common.cache.AbstractGenericCache;

/**
 * Cache to store user information.
 *
 * @author joaquin.romero
 */
public class MemoryUserCache extends AbstractGenericCache<String, UserDetails> implements UserCache {

	/**
	 * Instantiates a new memory user cache.
	 */
	public MemoryUserCache() {
		super();
	}

	/**
	 * Instantiates a new memory user cache.
	 *
	 * @param ttl
	 *            the ttl
	 */
	public MemoryUserCache(long ttl) {
		super(ttl);
	}

	/* (non-Javadoc)
	 * @see com.ontimize.jee.common.cache.GenericCache#requestData(java.lang.Object)
	 */
	@Override
	protected UserDetails requestData(String key) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserCache#getUserFromCache(java.lang.String)
	 */
	@Override
	public UserDetails getUserFromCache(String username) {
		return this.get(username);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserCache#putUserInCache(org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public void putUserInCache(UserDetails user) {
		this.put(user.getUsername(), user);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserCache#removeUserFromCache(java.lang.String)
	 */
	@Override
	public void removeUserFromCache(String username) {
		this.invalidateCache(username);
	}

}
