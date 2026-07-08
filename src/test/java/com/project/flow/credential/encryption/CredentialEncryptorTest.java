package com.project.flow.credential.encryption;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CredentialEncryptorTest {

    private CredentialEncryptor credentialEncryptor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Use a stable jwtSecret and no custom rawKey
        credentialEncryptor = new CredentialEncryptor(null, "my_super_secret_jwt_key_which_is_extremely_long", objectMapper);
    }

    @Test
    void shouldEncryptAndDecryptSecretsSuccessfully() {
        // Arrange
        Map<String, String> originalSecrets = new HashMap<>();
        originalSecrets.put("token", "ghp_1234567890abcdefghijklmnopqrstuvwxyz");
        originalSecrets.put("username", "testuser");

        // Act
        String encrypted = credentialEncryptor.encrypt(originalSecrets);
        assertNotNull(encrypted);
        assertNotEquals("", encrypted);
        assertFalse(encrypted.contains("ghp_1234567890")); // Verify not stored in plain text

        Map<String, String> decryptedSecrets = credentialEncryptor.decrypt(encrypted);

        // Assert
        assertEquals(originalSecrets.size(), decryptedSecrets.size());
        assertEquals("ghp_1234567890abcdefghijklmnopqrstuvwxyz", decryptedSecrets.get("token"));
        assertEquals("testuser", decryptedSecrets.get("username"));
    }

    @Test
    void shouldFailDecryptionWhenDataIsTampered() {
        // Arrange
        Map<String, String> originalSecrets = Map.of("token", "secret-value");
        String encrypted = credentialEncryptor.encrypt(originalSecrets);

        // Tamper with the encrypted string
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "AAAA";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> credentialEncryptor.decrypt(tampered));
    }
}
