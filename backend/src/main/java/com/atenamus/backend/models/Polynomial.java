package com.atenamus.backend.models;

import it.unisa.dia.gas.jpbc.Element;

public class Polynomial {
  public int deg;
  /* coefficients from [0] x^0 to [deg] x^deg */
  public Element[] coef; /* G_T (of length deg+1) */
}
