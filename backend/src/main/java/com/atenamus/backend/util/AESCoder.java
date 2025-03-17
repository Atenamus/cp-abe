package com.atenamus.backend.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class AESCoder {

  /**
   * Process raw bytes to ensure they have the correct length for AES-128 (16
   * bytes)
   * 
   * @param rawKey Raw bytes that might not have the correct AES key length
   * @return A properly sized 16-byte AES key
   */
  private static byte[] processKeyForAES(byte[] rawKey) {
    // For AES-128, we need exactly 16 bytes
    byte[] aesKey = new byte[16];

    if (rawKey.length >= 16) {
      // If input is >= 16 bytes, use the first 16 bytes
      System.arraycopy(rawKey, 0, aesKey, 0, 16);
    } else {
      // If input is < 16 bytes, copy what we have and zero-pad the rest
      System.arraycopy(rawKey, 0, aesKey, 0, rawKey.length);
      // The remaining bytes will be zeros by default
    }

    return aesKey;
  }

  /**
   * Encrypts the given plaintext using AES encryption with the provided key
   *
   * @param key       the AES key to use for encryption (will be processed to 16
   *                  bytes if needed)
   * @param plaintext the plaintext to be encrypted
   * @return the encrypted byte array
   */
  public static byte[] encrypt(byte[] key, byte[] plaintext) throws Exception {
    // Process the key to ensure it's exactly 16 bytes (128 bits) for AES-128
    byte[] aesKey = processKeyForAES(key);
    System.out.println("Processed AES key for encryption, hash: " + Arrays.hashCode(aesKey));

    SecretKeySpec skeySpec = new SecretKeySpec(aesKey, "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
    return cipher.doFinal(plaintext);
  }

  /**
   * Decrypts the given ciphertext using AES decryption with the provided key
   *
   * @param key        the AES key to use for decryption (will be processed to 16
   *                   bytes if needed)
   * @param ciphertext the ciphertext to be decrypted
   * @return the decrypted byte array
   */
  public static byte[] decrypt(byte[] key, byte[] ciphertext) throws Exception {
    // Process the key to ensure it's exactly 16 bytes (128 bits) for AES-128
    byte[] aesKey = processKeyForAES(key);
    System.out.println("Processed AES key for decryption, hash: " + Arrays.hashCode(aesKey));

    SecretKeySpec skeySpec = new SecretKeySpec(aesKey, "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    return cipher.doFinal(ciphertext);
  }
}
