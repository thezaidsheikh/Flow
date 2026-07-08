package com.project.flow.credential.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.flow.common.exception.InternalServerError;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service to execute AES-256 GCM encryption and decryption of credential secrets map.
 * Cryptographic secrets are securely encrypted at rest using unique IVs.
 */
@Component
public class CredentialEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final SecretKeySpec secretKeySpec;
    private final ObjectMapper objectMapper;

    public CredentialEncryptor(
            @Value("${encryption.key:#{null}}") String rawKey,
            @Value("${jwt.secret}") String jwtSecret,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Derive a secure 256-bit AES key from the encryption key or fall back to jwt secret using SHA-256 hashing
        byte[] keyBytes = deriveKey(rawKey != null ? rawKey : jwtSecret);
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] deriveKey(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Encrypt a map of secret values into a Base64-encoded string combining IV and ciphertext.
     *
     * @param secrets the map of string secrets (e.g. key-token pairs)
     * @return Base64-encoded encrypted representation
     */
    public String encrypt(Map<String, String> secrets) {
        try {
            String json = objectMapper.writeValueAsString(secrets);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);

            byte[] cipherText = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (JsonProcessingException e) {
            throw new InternalServerError("Failed to serialize secrets for encryption", e, Optional.empty());
        } catch (GeneralSecurityException e) {
            throw new InternalServerError("Encryption failed", e, Optional.empty());
        }
    }

    /**
     * Decrypt an encrypted Base64-encoded string back into the original secrets map.
     *
     * @param encryptedBase64 Base64-encoded encrypted representation containing IV and ciphertext
     * @return original map of secrets
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> decrypt(String encryptedBase64) {
        try {
            byte[] ivCipherConcat = Base64.getDecoder().decode(encryptedBase64);

            ByteBuffer byteBuffer = ByteBuffer.wrap(ivCipherConcat);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherText);
            String json = new String(decryptedBytes, StandardCharsets.UTF_8);

            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new InternalServerError("Failed to deserialize decrypted secrets", e, Optional.empty());
        } catch (GeneralSecurityException e) {
            throw new InternalServerError("Decryption failed", e, Optional.empty());
        }
    }
}
