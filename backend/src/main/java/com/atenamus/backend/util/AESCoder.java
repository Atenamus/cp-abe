package com.atenamus.backend.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder {

  /**
   * 
   * get a 32-byte key regardless of input size.
   *
   * @param seed the seed bytes to generate the key
   * @return the SecretKeySpec for AES
   */
  private static byte[] generateRawKey(byte[] seed) throws Exception {
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    sr.setSeed(seed);
    kgen.init(128, sr);
    SecretKey skey = kgen.generateKey();
    return skey.getEncoded();
  }

  /**
   * Encrypts the given plaintext using AES encryption with the provided seed.
   *
   * @param seed the seed to generate the AES key
   * @param plaintext the plaintext to be encrypted
   * @return the encrypted byte array
   */
  public static byte[] encrypt(byte[] seed, byte[] plaintext) throws Exception {
    byte[] rawKey = generateRawKey(seed);
    SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
    return cipher.doFinal(plaintext);
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
    byte[] rawKey = generateRawKey(seed);
    SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    return cipher.doFinal(ciphertext);
  }
}
