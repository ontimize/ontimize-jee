package com.ontimize.jee.server.security.encrypt;

import java.security.MessageDigest;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import com.ontimize.jee.common.naming.I18NNaming;
import com.ontimize.jee.common.util.Base64Utils;

/**
 * The Class PasswordEncryptHelper.
 */
@Component("PasswordEncryptHelper")
@Lazy(value = true)
public class PasswordEncryptHelper implements IPasswordEncryptHelper {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryptHelper.class);

    @Override
    public Map<?, ?> encryptMap(String columnName, Map<?, ?> keysValues) {
        ((Map<Object, Object>) keysValues).computeIfPresent(columnName, (k, v) -> this.encrypt((String) v));
        return keysValues;
    }

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

    @Override
    public void checkPasswords(String storedPass, Object pass) {
        final boolean encrypted = this.isHash(storedPass);
        if ((pass == null) || (encrypted && !storedPass.equals(this.encrypt(pass.toString())))
                || (!encrypted && !storedPass.equals(pass))) {
            logger.error("Authorization denied!");
            throw new AuthenticationCredentialsNotFoundException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);
        }
    }

    private boolean isHash(String string) {
        final int length = string.length();
        return length >= 28 && length % 4 == 0 && Base64.isBase64(string);
    }
}
