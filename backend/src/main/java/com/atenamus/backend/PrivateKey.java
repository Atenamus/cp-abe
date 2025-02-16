package com.atenamus.backend;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

public class PrivateKey {
    /*
     * A private key
     */
    public Element d; /* G_2 */
    public ArrayList<PrivateKeyComp> comps; /* BswabePrvComp */

}
