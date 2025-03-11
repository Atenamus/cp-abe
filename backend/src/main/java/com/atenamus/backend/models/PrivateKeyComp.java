package com.atenamus.backend.models;

import it.unisa.dia.gas.jpbc.Element;

public class PrivateKeyComp {
    /* these actually get serialized */
    public String attr;
    public Element d; /* G_2 */
    public Element dp; /* G_2 */

    /* only used during dec */
    int used;
    public Element z; /* G_1 */
    public Element zp; /* G_1 */

}
