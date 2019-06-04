package com.ontimize.jee.common.security;

import java.net.ConnectException;
import java.net.URI;

import com.ontimize.jee.common.exceptions.InvalidCredentialsException;

/**
 * The Interface ILoginProvider.
 */
public interface ILoginProvider {

	/**
	 * Do login.
	 *
	 * @param uri
	 *            the uri
	 * @param user
	 *            the user
	 * @param password
	 *            the password
	 * @return the user information
	 * @throws InvalidCredentialsException
	 *             the invalid credentials exception
	 * @throws ConnectException
	 */
	void doLogin(URI uri, String user, String password) throws InvalidCredentialsException, ConnectException;

	/**
	 * Do login. Use last used credentials to login
	 *
	 * @param uri
	 *            the uri
	 * @return the user information
	 * @throws InvalidCredentialsException
	 *             the invalid credentials exception
	 */
	void doLogin(URI uri) throws InvalidCredentialsException, ConnectException;

}
