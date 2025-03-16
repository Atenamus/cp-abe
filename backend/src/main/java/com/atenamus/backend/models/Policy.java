package com.atenamus.backend.models;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

public class Policy {
	/* serialized */

	/* k=1 if leaf, otherwise threshould */
	public int k;
	/* attribute string if leaf, otherwise null */
	public String attr;
	public Element c; /* G_1 only for leaves */
	public Element cp; /* G_1 only for leaves */
	/* array of BswabePolicy and length is 0 for leaves */
	public Policy[] children;

	/* only used during encryption */
	public Polynomial q;

	/* only used during decription */
	public boolean satisfiable;
	public int min_leaves;
	public int attri;
	public ArrayList<Integer> satl = new ArrayList<Integer>();
}
