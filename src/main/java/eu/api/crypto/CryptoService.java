package eu.api.crypto;

/**
 * Application-level encryption for sensitive fields (e.g. workplace).
 * AES-GCM; key from env (CRYPTO_MASTER_KEY base64).
 */
public interface CryptoService {

    /**
     * Encrypts plaintext. Returns null if input is null; empty string encrypted for storage.
     */
    String encrypt(String plaintext);

    /**
     * Decrypts ciphertext produced by encrypt. Returns null if input is null or empty.
     */
    String decrypt(String ciphertext);
}
