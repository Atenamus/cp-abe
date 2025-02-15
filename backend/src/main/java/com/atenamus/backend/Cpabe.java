package com.atenamus.backend;

import java.io.ByteArrayInputStream;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class Cpabe {

    private static final String curveParams = "type a\n"
            + "q 87807107996633125224377819847540498158068831994142082"
            + "1102865339926647563088022295707862517942266222142315585"
            + "8769582317459277713367317481324925129998224791\n"
            + "h 12016012264891146079388821366740534204802954401251311"
            + "822919615131047207289359704531102844802183906537786776\n"
            + "r 730750818665451621361119245571504901405976559617\n"
            + "exp2 159\n"
            + "exp1 107\n"
            + "sign1 1\n"
            + "sign0 1\n";

    public void setup(PublicKey pub, MasterSecretKey msk) {
        Element alpha, beta_inv;

        // Load pairing parameters
        PropertiesParameters params = new PropertiesParameters();
        params.load(new ByteArrayInputStream(curveParams.getBytes()));

        // Initialize pairing
        pub.pairingDesc = curveParams;
        pub.p = PairingFactory.getPairing(params);
        Pairing pairing = pub.p;

        // Initialize public key elements
        pub.g = pairing.getG1().newElement();
        pub.f = pairing.getG1().newElement();
        pub.h = pairing.getG1().newElement();
        pub.gp = pairing.getG2().newElement();
        pub.g_hat_alpha = pairing.getGT().newElement();

        // Initialize master secret key elements
        alpha = pairing.getZr().newElement();
        msk.beta = pairing.getZr().newElement();
        msk.g_alpha = pairing.getG2().newElement();

        // Set random values
        alpha.setToRandom();
        msk.beta.setToRandom();
        pub.g.setToRandom();
        pub.gp.setToRandom();

        // Compute g_alpha = gp^alpha
        msk.g_alpha = pub.gp.duplicate();
        msk.g_alpha.powZn(alpha);

        // Compute beta inverse
        beta_inv = msk.beta.duplicate();
        beta_inv.invert();

        // Compute f = g^(1/beta)
        pub.f = pub.g.duplicate();
        pub.f.powZn(beta_inv);

        // Compute h = g^beta
        pub.h = pub.g.duplicate();
        pub.h.powZn(msk.beta);

        // Compute e(g, g_alpha) (g_hat_alpha)
        pub.g_hat_alpha = pairing.pairing(pub.g, msk.g_alpha);
    }
}
