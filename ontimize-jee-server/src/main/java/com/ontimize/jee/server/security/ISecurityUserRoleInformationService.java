package com.ontimize.jee.server.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public interface ISecurityUserRoleInformationService {

    Collection<GrantedAuthority> loadUserRoles(String userLogin);

}
