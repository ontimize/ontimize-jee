package com.ontimize.jee.server.security.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncryptHelperTest {

    @Test
    @DisplayName("Encrypt password in a map → SHA")
    void encryptMap() {
        String plainPass1 = "demouser";
        String plainPass2 = "admin";
        String columnName = "passColumn";
        String encryptedPass1Result = "Obq8MytBJgQGZkSolNn0e4/irUI=";
        String encryptedPass2Result = "0DPiKuNIrrVmD8IUCuw1hQxNqZc=";

        PasswordEncryptHelper pwe = new PasswordEncryptHelper();
        Map<String, Object> map = new HashMap<>();
        map.put(columnName, plainPass1);
        pwe.encryptMap(columnName, map);
        assertEquals(encryptedPass1Result, map.get(columnName), "Failed encrypt!");
        map.put(columnName, plainPass2);
        pwe.encryptMap(columnName, map);
        assertEquals(encryptedPass2Result, map.get(columnName), "Failed encrypt!");
    }

    @Test
    @DisplayName("Encrypt password → SHA")
    void encrypt() {
        String plainPass1 = "demouser";
        String plainPass2 = "admin";
        String encryptedPass1Result = "Obq8MytBJgQGZkSolNn0e4/irUI=";
        String encryptedPass2Result = "0DPiKuNIrrVmD8IUCuw1hQxNqZc=";

        PasswordEncryptHelper pwe = new PasswordEncryptHelper();
        String encryptedPass1 = pwe.encrypt(plainPass1);
        String encryptedPass2 = pwe.encrypt(plainPass2);

        assertEquals(encryptedPass1Result, encryptedPass1, "Failed encrypt!");
        assertEquals(encryptedPass2Result, encryptedPass2, "Failed encrypt!");
    }
}