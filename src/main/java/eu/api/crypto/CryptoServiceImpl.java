package eu.api.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class CryptoServiceImpl implements CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int AES_KEY_LENGTH_BYTES = 32;

    private final SecretKey key;

    public CryptoServiceImpl(@Value("${crypto.master-key-base64:}") String masterKeyBase64) {
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            log.warn("CRYPTO_MASTER_KEY not set; using placeholder key. Set CRYPTO_MASTER_KEY in production.");
            byte[] placeholder = new byte[AES_KEY_LENGTH_BYTES];
            new SecureRandom().nextBytes(placeholder);
            this.key = new SecretKeySpec(placeholder, "AES");
        } else {
            byte[] decoded = Base64.getDecoder().decode(masterKeyBase64.trim());
            if (decoded.length < AES_KEY_LENGTH_BYTES) {
                throw new IllegalStateException("CRYPTO_MASTER_KEY must be base64-encoded at least 32 bytes (256 bits)");
            }
            byte[] keyBytes = decoded.length == AES_KEY_LENGTH_BYTES
                    ? decoded
                    : java.util.Arrays.copyOf(decoded, AES_KEY_LENGTH_BYTES);
            this.key = new SecretKeySpec(keyBytes, "AES");
        }
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        if (plaintext.isEmpty()) {
            return "";
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            if (combined.length <= GCM_IV_LENGTH_BYTES) {
                return null;
            }
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Decryption failed (wrong key or tampered data)");
            return null;
        }
    }
}
