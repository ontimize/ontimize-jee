package com.ontimize.jee.server.security;

import com.ontimize.jee.server.security.authorization.ISecurityAuthorizator;

/**
 * The Class SecurityConfiguration.
 */
public class SecurityConfiguration {

	/** The authorizators. */
	private ISecurityAuthorizator				authorizator;
	private ISecurityUserInformationService		userInformationService;
	private ISecurityUserRoleInformationService	userRoleInformationService;
	private ISecurityRoleInformationService		roleInformationService;

	/**
	 * Instantiates a new security configuration.
	 */
	public SecurityConfiguration() {
		super();
	}

	/**
	 * @return the authorizator
	 */
	public ISecurityAuthorizator getAuthorizator() {
		return this.authorizator;
	}

	/**
	 * @param authorizator
	 *            the authorizator to set
	 */
	public void setAuthorizator(ISecurityAuthorizator authorizator) {
		this.authorizator = authorizator;
	}

	/**
	 * @return the userInformationService
	 */
	public ISecurityUserInformationService getUserInformationService() {
		return this.userInformationService;
	}

	/**
	 * @param userInformationService
	 *            the userInformationService to set
	 */
	public void setUserInformationService(ISecurityUserInformationService userInformationService) {
		this.userInformationService = userInformationService;
	}

	/**
	 * @return the userRoleInformationService
	 */
	public ISecurityUserRoleInformationService getUserRoleInformationService() {
		return this.userRoleInformationService;
	}

	/**
	 * @param userRoleInformationService
	 *            the userRoleInformationService to set
	 */
	public void setUserRoleInformationService(ISecurityUserRoleInformationService userRoleInformationService) {
		this.userRoleInformationService = userRoleInformationService;
	}

	/**
	 * @return the roleInformationService
	 */
	public ISecurityRoleInformationService getRoleInformationService() {
		return this.roleInformationService;
	}

	/**
	 * @param roleInformationService
	 *            the roleInformationService to set
	 */
	public void setRoleInformationService(ISecurityRoleInformationService roleInformationService) {
		this.roleInformationService = roleInformationService;
	}

}
