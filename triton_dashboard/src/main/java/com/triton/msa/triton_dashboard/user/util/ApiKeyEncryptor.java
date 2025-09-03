package com.triton.msa.triton_dashboard.user.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ApiKeyEncryptor {

    private final AesBytesEncryptor encryptor;

    public ApiKeyEncryptor(@Value("${api.key.encryption.password}") String password,
                           @Value("${api.key.encryption.salt}") String salt) {
        this.encryptor = new AesBytesEncryptor(password, salt);
    }

    public String encrypt(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        byte[] encrypt = encryptor.encrypt(apiKey.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypt);
    }

    public String decrypt(String encryptedApiKey) {
        if (encryptedApiKey == null) {
            return null;
        }
        byte[] decryptBytes = Base64.getDecoder().decode(encryptedApiKey);
        byte[] decrypted = encryptor.decrypt(decryptBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
