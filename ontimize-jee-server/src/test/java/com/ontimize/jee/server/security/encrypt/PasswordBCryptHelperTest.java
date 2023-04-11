package com.ontimize.jee.server.security.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordBCryptHelperTest {

    @Test
    @DisplayName("Encrypt password in a map → BCrypt")
    void encryptMap() {
        String plainPass1 = "demouser";
        String plainPass2 = "admin";
        String columnName = "passColumn";

        PasswordBCryptHelper pwb = new PasswordBCryptHelper();
        Map<String, Object> map = new HashMap<>();
        map.put(columnName, plainPass1);
        pwb.encryptMap(columnName, map);
        assertTrue(pwb.checkHash(plainPass1, (String) map.get(columnName)), "Hash not match!");
        map.put(columnName, plainPass2);
        pwb.encryptMap(columnName, map);
        assertTrue(pwb.checkHash(plainPass2, (String) map.get(columnName)), "Hash not match!");
    }

    @Test
    @DisplayName("Encrypt password → BCrypt")
    void encrypt() {
        String plainPass1 = "demouser";
        String plainPass2 = "admin";

        PasswordBCryptHelper pwb = new PasswordBCryptHelper();
        String encryptedPass1 = pwb.encrypt(plainPass1);
        String encryptedPass2 = pwb.encrypt(plainPass2);

        assertTrue(pwb.checkHash(plainPass1,encryptedPass1), "Hash not match!");
        assertTrue(pwb.checkHash(plainPass2,encryptedPass2), "Hash not match!");
        assertFalse(pwb.checkHash(plainPass2,encryptedPass1), "Hash matched!");
        assertFalse(pwb.checkHash(plainPass1,encryptedPass2), "Hash matched!");

    }

    @Test
    @DisplayName("Check if password matches a hash")
    void checkHash() {
        String plainPass1 = "demouser";
        String plainPass2 = "admin";
        String storedPass1 = "$2a$12$l8Eu1j3kXNXPvwXXIad/ROpIFmVERZmKKuhKe222PlvcTfANpMdDi";
        String storedPass2 = "$2a$12$YGfxyoq6xOks13pb15TO7up6f9TgEfImOW4fcc/dClUeKc65eG0ka";

        PasswordBCryptHelper pwb = new PasswordBCryptHelper();

        assertTrue(pwb.checkHash(plainPass1,storedPass1), "Hash not match!");
        assertTrue(pwb.checkHash(plainPass2,storedPass2), "Hash not match!");
        assertFalse(pwb.checkHash(plainPass1,storedPass2), "Hash matched!");
        assertFalse(pwb.checkHash(plainPass2,storedPass1), "Hash matched!");
    }

    @Test
    @DisplayName("Check if password is equals to encrypted (or not) password")
    void checkPasswords() {
        String plainPass1 = "demouser";
        String plainPass2 = "admin";
        String storedPass1 = "$2a$12$0T662RATA3mcJjFIDBpSKOGCuz6TvKdwlsTpy6pA/hKY4Y2KUoG22";
        String storedPass2 = "$2a$12$tTXd.6B6taXiStY36sqG2OTVNm7qpzf4erjgEYf8UutzjzXmHIClW";

        PasswordBCryptHelper pwb = new PasswordBCryptHelper();
        assertDoesNotThrow(()->pwb.checkPasswords(storedPass1,plainPass1), "Check failed!");
        assertDoesNotThrow(()->pwb.checkPasswords(storedPass2,plainPass2), "Check failed!");
        assertDoesNotThrow(()->pwb.checkPasswords(plainPass1,plainPass1), "Check failed!");
        assertDoesNotThrow(()->pwb.checkPasswords(plainPass2,plainPass2), "Check failed!");
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()->pwb.checkPasswords(plainPass1,null), "Check failed!");
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()->pwb.checkPasswords(storedPass2,plainPass1), "Check failed!");
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()->pwb.checkPasswords(storedPass1,plainPass2), "Check failed!");
    }
}