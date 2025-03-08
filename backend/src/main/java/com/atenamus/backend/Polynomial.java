package com.atenamus.backend;

import it.unisa.dia.gas.jpbc.Element;

public class Polynomial {
  int deg;
  /* coefficients from [0] x^0 to [deg] x^deg */
  Element[] coef; /* G_T (of length deg+1) */
}
