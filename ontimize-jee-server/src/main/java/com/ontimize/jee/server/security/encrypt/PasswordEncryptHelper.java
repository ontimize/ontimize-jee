package com.ontimize.jee.server.security.encrypt;

import java.security.MessageDigest;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ontimize.jee.common.util.Base64Utils;

/**
 * The Class PasswordEncryptHelper.
 */
@Component("PasswordEncryptHelper")
@Lazy(value = true)
public class PasswordEncryptHelper implements IPasswordEncryptHelper {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryptHelper.class);

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.security.IPasswordEncryptHelper#encryptMap(java.lang.String,
     * java.util.Map)
     */
    @Override
    public Map<?, ?> encryptMap(String columnName, Map<?, ?> keysValues) {
        Object value = keysValues.get(columnName);
        if (value != null) {
            ((Map<Object, Object>) keysValues).put(columnName, this.encrypt((String) value));
        }
        return keysValues;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.security.IPasswordEncryptHelper#encrypt(java.lang.String)
     */
    @Override
    public String encrypt(String password) {
        try {
            MessageDigest md = java.security.MessageDigest.getInstance("SHA");
            // Get the password byes
            byte[] bytes = password.getBytes();
            md.update(bytes);
            byte[] ecriptedBytes = md.digest();

            char[] characters = Base64Utils.encode(ecriptedBytes);
            String result = new String(characters);
            return result;
        } catch (Exception e) {
            PasswordEncryptHelper.logger.error(null, e);
            return null;
        }
    }

}
