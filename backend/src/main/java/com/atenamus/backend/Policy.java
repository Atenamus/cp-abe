package com.atenamus.backend;

import java.util.ArrayList;
import it.unisa.dia.gas.jpbc.Element;

public class Policy {
  /* serialized */
	
	/* k=1 if leaf, otherwise threshould */
	int k;
	/* attribute string if leaf, otherwise null */
	String attr;
	Element c;			/* G_1 only for leaves */
	Element cp;		/* G_1 only for leaves */
	/* array of BswabePolicy and length is 0 for leaves */
	Policy[] children;
	
	/* only used during encryption */
	Polynomial q;

	/* only used during decription */
	boolean satisfiable;
	int min_leaves;
	int attri;
	ArrayList<Integer> satl = new ArrayList<Integer>();
}
