package com.ontimize.jee.server.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ontimize.jee.server.security.authorization.AutoRefreshRoleProvider;

/**
 * Cache to store user information.
 */
public class AutoRefreshMemoryUserCache implements UserCache {
	/** The CONSTANT logger */
	private static final Logger	logger	= LoggerFactory.getLogger(AutoRefreshRoleProvider.class);

	/**
	 * The application context (autowired).
	 */
	@Autowired
	private ApplicationContext	context;

	@Autowired
	private UserDetailsService			userDetailService;

	/**
	 * The delay to wait between refresh invocations;
	 */
	protected long				delay;

	/**
	 * The cache.
	 */
	protected Map<String, UserDetails>	cache;

	/**
	 * Instantiates a new memory user cache.
	 */
	public AutoRefreshMemoryUserCache() {
		this(0);
	}

	/**
	 * Instantiates a new memory user cache.
	 *
	 * @param delayMs
	 *            the delay in ms
	 */
	public AutoRefreshMemoryUserCache(long delayMs) {
		super();
		this.cache = new HashMap<String, UserDetails>();
		this.delay = delayMs;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserCache#getUserFromCache(java.lang.String)
	 */
	@Override
	public UserDetails getUserFromCache(String username) {
		UserDetails userDetails = this.cache.get(username);
		if (userDetails == null) {
			userDetails = this.loadUserAndCache(username);
		}
		return userDetails;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserCache#putUserInCache(org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public void putUserInCache(UserDetails user) {
		this.cache.put(user.getUsername(), user);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserCache#removeUserFromCache(java.lang.String)
	 */
	@Override
	public void removeUserFromCache(String username) {
		this.cache.remove(username);
	}

	protected UserDetails loadUserAndCache(String username) {
		UserDetails userDetails = this.userDetailService.loadUserByUsername(username);
		this.putUserInCache(userDetails);
		return userDetails;
	}


	/**
	 * Scheduled task to auto "refresh" roles configuration
	 */
	@Scheduled(fixedDelay = 10000)
	public void scheduleFixedDelayTask() {
		if (this.delay > 0) {
			try {
				Thread.sleep(this.delay);
			} catch (InterruptedException err) {
				AutoRefreshMemoryUserCache.logger.trace("Wait ignored");
			}
		}
		AutoRefreshMemoryUserCache.logger.trace("User refresh scheduled task starts.");
		List<String> refreshUsers = new ArrayList<>(this.cache.keySet());
		for (String userName : refreshUsers) {
			this.loadUserAndCache(userName);
		}
		AutoRefreshMemoryUserCache.logger.trace("Role refresh scheduled task ends.");
	}
}
