package com.ontimize.jee.server.security.encrypt;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.ontimize.jee.common.naming.I18NNaming;

public class PasswordBCryptHelper implements IPasswordEncryptHelper {

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(PasswordBCryptHelper.class);

    //Workload value to generate hashes. Between 10 and 31.
    private int workload = 12;

    public PasswordBCryptHelper() {
        workload = 12;
    }


    public PasswordBCryptHelper(int workload) {
        // This restriction is used to avoid lower security config and the next overflow vulnerability: https://devhub.checkmarx.com/cve-details/CVE-2022-22976/
        if (workload >= 31 || workload <= 10) {
            workload = workload >= 31 ? 31 : 10;
        }
        this.workload = workload;
    }


    @Override
    public Map<?, ?> encryptMap(String columnName, Map<?, ?> keysValues) {
        ((Map<Object, Object>) keysValues).computeIfPresent(columnName, (k, v) -> this.encrypt((String) v));
        return keysValues;
    }

    @Override
    public String encrypt(String pass) {
        String salt = BCrypt.gensalt(this.workload);
        return BCrypt.hashpw(pass, salt);
    }

    /**
     * This method can be used to verify a hash calculated from a pass against an existing hash.
     *
     * @param plainPass the plain password
     * @param hash the existing hash
     * @return boolean true if matches, false otherwise
     */
    public boolean checkHash(String plainPass, String hash) {
        try {
            return BCrypt.checkpw(plainPass, hash);
        }catch (IllegalArgumentException e){
            logger.debug("Hash string is not compatible with BCrypt structure");
            return false;
        }
    }

    @Override
    public void checkPasswords(String storedPass, Object pass) {
        final boolean encrypted = isHash(storedPass);
        if ((pass == null) || (encrypted && !this.checkHash(pass.toString(), storedPass))
                || (!encrypted && !storedPass.equals(pass))) {
            logger.error("Authorization denied!");
            throw new AuthenticationCredentialsNotFoundException(I18NNaming.E_AUTH_PASSWORD_NOT_MATCH);
        }
    }

    private boolean isHash(final String string) {
        return string.length() > 50 && string.startsWith("$2") && (string.charAt(2) == '$' || string.charAt(3) == '$');
    }
}


