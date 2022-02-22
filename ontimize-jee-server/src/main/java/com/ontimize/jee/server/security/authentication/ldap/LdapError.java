package com.ontimize.jee.server.security.authentication.ldap;

public interface LdapError {
	public static final String NO_LDAP_CONNECTION = "NO_CONNECT_TO_LDAP";
	public static final String ERROR_SEARCHING_IN_LDAP = "ERROR_SEARCHING_IN_LDAP";
	public static final String ERROR_LOGIN_LDAP = "LOGINEXCEPTION_WITH_LDAP";
	public static final String ERROR_IO_LDAP = "IOEXCEPTION_WITH_LDAP";
	public static final String EMPTY_LDAP_HOST = "HOST_CANNOT_BE_EMPTY";
	public static final String EMPTY_LDAP_USER = "USER_CANNOT_BE_EMPTY";
	public static final String EMPTY_LDAP_PASSWORD = "PASSWORD_CANNOT_BE_EMPTY";;
	public static final String LDAP_AUTH_USER_PASS_NOT_VALID = "LDAP_CREDENTIALS_NOT_VALID";

}
