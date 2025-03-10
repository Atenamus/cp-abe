package com.atenamus.backend.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder {

  /**
   * Generates a raw AES key from the given seed.
   *
   * @param seed the seed to generate the key
   * @return the raw AES key
   */
  private static byte[] getRawKey(byte[] seed) throws Exception {
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    sr.setSeed(seed); // Use the seed to initialize the random number generator
    kgen.init(128, sr); // Generate a 128-bit AES key
    SecretKey skey = kgen.generateKey();
    return skey.getEncoded(); // Return the raw key bytes
  }

  /**
   * Encrypts the given plaintext using AES encryption with the provided seed.
   *
   * @param seed      the seed to generate the AES key
   * @param plaintext the plaintext to be encrypted
   * @return the encrypted byte array
   */
  public static byte[] encrypt(byte[] seed, byte[] plaintext) throws Exception {
    byte[] raw = getRawKey(seed); // Generate the AES key
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES"); // Create a key specification
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Initialize the cipher
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec); // Set to encryption mode
    return cipher.doFinal(plaintext); // Encrypt the plaintext
  }

  /**
   * Decrypts the given ciphertext using AES decryption with the provided seed.
   *
   * @param seed       the seed to generate the AES key
   * @param ciphertext the ciphertext to be decrypted
   * @return the decrypted byte array
   * @throws Exception if any error occurs during decryption
   */
  public static byte[] decrypt(byte[] seed, byte[] ciphertext) throws Exception {
    byte[] raw = getRawKey(seed); // Generate the AES key
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES"); // Create a key specification
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Initialize the cipher
    cipher.init(Cipher.DECRYPT_MODE, skeySpec); // Set to decryption mode
    return cipher.doFinal(ciphertext); // Decrypt the ciphertext
  }
}