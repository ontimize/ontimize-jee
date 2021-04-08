package com.ontimize.jee.server.security;

import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CachedUserDetailsService implements UserDetailsService {

    private final UserCache userCache;

    private final UserDetailsService userDetailsService;

    public CachedUserDetailsService(UserCache userCache, UserDetailsService userDetailsService) {
        super();
        this.userCache = userCache;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = this.userCache == null ? null : this.userCache.getUserFromCache(username);
        if (user == null) {
            user = this.userDetailsService.loadUserByUsername(username);
            if (user != null) {
                this.userCache.putUserInCache(user);
            } else {
                throw new UsernameNotFoundException("User not found");
            }
        }
        return user;
    }

}
