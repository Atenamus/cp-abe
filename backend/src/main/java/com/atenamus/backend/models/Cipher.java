package com.atenamus.backend.models;

import it.unisa.dia.gas.jpbc.Element;

public class Cipher {
  public Element cs; /* G_T */
  public Element c; /* G_1 */
  public Policy p;

  // Encryption date (Y)
  public long encryptionDate; // Unix timestamp in milliseconds
}
