package com.atenamus.backend.models;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

public class PrivateKey {
    /*
     * A private key
     */
    public Element d; /* G_2 */
    public ArrayList<PrivateKeyComp> comps; /* BswabePrvComp */

    // Key traceability information
    public String userId; // User identifier
    public String userEmail; // User email
    public long timestamp; // Key generation timestamp

    // Key expiration date (X)
    public long expirationDate; // Unix timestamp in milliseconds
}
