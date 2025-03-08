package com.atenamus.backend;

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
   * @throws Exception if any error occurs during key generation
   */
  private static byte[] getRawKey(byte[] seed) throws Exception {
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    sr.setSeed(seed);
    kgen.init(128, sr); // 192 and 256 bits may not be available
    SecretKey skey = kgen.generateKey();
    byte[] raw = skey.getEncoded();
    return raw;
  }

  /**
   * Encrypts the given plaintext using AES encryption with the provided seed.
   *
   * @param seed the seed to generate the AES key
   * @param plaintext the plaintext to be encrypted
   * @return the encrypted byte array
   * @throws Exception if any error occurs during encryption
   */
  public static byte[] encrypt(byte[] seed, byte[] plaintext) throws Exception {
    byte[] raw = getRawKey(seed);
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
    byte[] encrypted = cipher.doFinal(plaintext);
    return encrypted;
  }

  /**
   * Decrypts the given ciphertext using AES decryption with the provided seed.
   *
   * @param seed the seed to generate the AES key
   * @param ciphertext the ciphertext to be decrypted
   * @return the decrypted byte array
   * @throws Exception if any error occurs during decryption
   */
  public static byte[] decrypt(byte[] seed, byte[] ciphertext) throws Exception {
    byte[] raw = getRawKey(seed);
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    byte[] decrypted = cipher.doFinal(ciphertext);

    return decrypted;
  }

}
