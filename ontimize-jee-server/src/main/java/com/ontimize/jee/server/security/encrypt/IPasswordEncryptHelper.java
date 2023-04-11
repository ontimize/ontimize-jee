package com.ontimize.jee.server.security.encrypt;

import java.util.Map;

/**
 * The Interface IPasswordEncryptHelper.
 */
public interface IPasswordEncryptHelper {

    /**
     * Encrypts a value in a map, given the name of the column and the map itself.
     *
     * @param columnName the name of the column to encrypt.
     * @param keysValues the map containing the keys and values.
     * @return the updated map with the encrypted value.
     */
    Map<?, ?> encryptMap(String columnName, Map<?, ?> keysValues);

    /**
     * Encrypts a password.
     *
     * @param pass the password to be encrypted
     * @return the encrypted password
     */
    String encrypt(String pass);

    /**
     * Performs password verification for authentication. Stored password can be encrypted or not.
     * @param storedPass password stored in a secure format.
     * @param pass user-supplied password in a non-secure format.
     */
    void checkPasswords(String storedPass, Object pass);
}
