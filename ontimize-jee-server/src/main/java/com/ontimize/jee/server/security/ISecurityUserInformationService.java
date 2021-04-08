package com.ontimize.jee.server.security;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.services.user.UserInformation;

public interface ISecurityUserInformationService extends UserDetailsService {

    public static final String BEAN_NAME = "databaseUserInformationService";

    public List<UserInformation> getAllUserInformation() throws OntimizeJEEException;

    public List<String> getAllUserInformationLogin() throws OntimizeJEEException;

}
