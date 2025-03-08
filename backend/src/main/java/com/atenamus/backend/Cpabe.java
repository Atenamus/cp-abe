package com.atenamus.backend;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class Cpabe {

    private static final String curveParams =
            "type a\n" + "q 87807107996633125224377819847540498158068831994142082"
                    + "1102865339926647563088022295707862517942266222142315585"
                    + "8769582317459277713367317481324925129998224791\n"
                    + "h 12016012264891146079388821366740534204802954401251311"
                    + "822919615131047207289359704531102844802183906537786776\n"
                    + "r 730750818665451621361119245571504901405976559617\n" + "exp2 159\n"
                    + "exp1 107\n" + "sign1 1\n" + "sign0 1\n";

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

    /*
     * Generate a private key with the given set of attributes.
     */
    public PrivateKey keygen(PublicKey pub, MasterSecretKey msk, String[] attrs)
            throws NoSuchAlgorithmException {
        PrivateKey prv = new PrivateKey();
        Element g_r, r, beta_inv;
        Pairing pairing;

        /* initialize */
        pairing = pub.p;
        prv.d = pairing.getG2().newElement();
        g_r = pairing.getG2().newElement();
        r = pairing.getZr().newElement();
        beta_inv = pairing.getZr().newElement();

        /* compute */
        r.setToRandom();
        g_r = pub.gp.duplicate();
        g_r.powZn(r);

        prv.d = msk.g_alpha.duplicate();
        prv.d.mul(g_r);
        beta_inv = msk.beta.duplicate();
        beta_inv.invert();
        prv.d.powZn(beta_inv);

        int i, len = attrs.length;
        prv.comps = new ArrayList<PrivateKeyComp>();
        for (i = 0; i < len; i++) {
            PrivateKeyComp comp = new PrivateKeyComp();
            Element h_rp;
            Element rp;

            comp.attr = attrs[i];

            comp.d = pairing.getG2().newElement();
            comp.dp = pairing.getG1().newElement();
            h_rp = pairing.getG2().newElement();
            rp = pairing.getZr().newElement();

            elementFromString(h_rp, comp.attr);
            rp.setToRandom();

            h_rp.powZn(rp);

            comp.d = g_r.duplicate();
            comp.d.mul(h_rp);
            comp.dp = pub.g.duplicate();
            comp.dp.powZn(rp);

            prv.comps.add(comp);
        }

        return prv;
    }

    /**
     * Encrypt a message under the specified policy using CP-ABE.
     * 
     * This method generates a random symmetric key and encrypts it using the CP-ABE scheme. The
     * actual message should be encrypted separately using the returned symmetric key.
     * 
     * @param pub The public parameters for the CP-ABE scheme
     * @param policy Access policy string (e.g., "admin AND (finance OR hr)")
     * @return A key-ciphertext pair containing the CP-ABE ciphertext and symmetric key
     * @throws Exception If policy parsing or encryption operations fail
     */

    public CipherKey enc(PublicKey pub, String policy) throws Exception {
        CipherKey cipherKey = new CipherKey();
        Cipher cph = new Cipher();
        Element s, m;

        /* initialize */
        Pairing pairing = pub.p;
        s = pairing.getZr().newElement();
        m = pairing.getGT().newElement();
        cph.cs = pairing.getGT().newElement();
        cph.c = pairing.getG1().newElement();
        cph.p = parsePolicyPostfix(policy);

        /* compute */
        m.setToRandom();
        s.setToRandom();
        cph.cs = pub.g_hat_alpha.duplicate();
        cph.cs.powZn(s); /* num_exps++; */
        cph.cs.mul(m); /* num_muls++; */

        cph.c = pub.h.duplicate();
        cph.c.powZn(s); /* num_exps++; */

        fillPolicy(cph.p, pub, s);

        cipherKey.cph = cph;
        cipherKey.key = m;

        return cipherKey;
    }

    private static void fillPolicy(Policy p, PublicKey pub, Element e)
            throws NoSuchAlgorithmException {
        int i;
        Element r, t, h;
        Pairing pairing = pub.p;
        r = pairing.getZr().newElement();
        t = pairing.getZr().newElement();
        h = pairing.getG2().newElement();

        p.q = randPoly(p.k - 1, e);

        if (p.children == null || p.children.length == 0) {
            p.c = pairing.getG1().newElement();
            p.cp = pairing.getG2().newElement();

            elementFromString(h, p.attr);
            p.c = pub.g.duplicate();;
            p.c.powZn(p.q.coef[0]);
            p.cp = h.duplicate();
            p.cp.powZn(p.q.coef[0]);
        } else {
            for (i = 0; i < p.children.length; i++) {
                r.set(i + 1);
                evalPoly(t, p.q, r);
                fillPolicy(p.children[i], pub, t);
            }
        }
    }

    private static void evalPoly(Element r, Polynomial q, Element x) {
        int i;
        Element s, t;

        s = r.duplicate();
        t = r.duplicate();

        r.setToZero();
        t.setToOne();

        for (i = 0; i < q.deg + 1; i++) {
            /* r += q->coef[i] * t */
            s = q.coef[i].duplicate();
            s.mul(t);
            r.add(s);
            /* t *= x */
            t.mul(x);
        }

    }

    private static Polynomial randPoly(int deg, Element zeroVal) {
        int i;
        Polynomial q = new Polynomial();
        q.deg = deg;
        q.coef = new Element[deg + 1];

        for (i = 0; i < deg + 1; i++)
            q.coef[i] = zeroVal.duplicate();
        q.coef[0].set(zeroVal);

        for (i = 1; i < deg + 1; i++)
            q.coef[i].setToRandom();

        return q;
    }

    private static Policy parsePolicyPostfix(String s) throws Exception {
        String[] toks;
        String tok;
        ArrayList<Policy> stack = new ArrayList<Policy>();
        Policy root;

        toks = s.split(" ");

        int toks_cnt = toks.length;
        for (int index = 0; index < toks_cnt; index++) {
            int i, k, n;

            tok = toks[index];
            if (!tok.contains("of")) {
                stack.add(baseNode(1, tok));
            } else {
                Policy node;

                /* parse kof n node */
                String[] k_n = tok.split("of");
                k = Integer.parseInt(k_n[0]);
                n = Integer.parseInt(k_n[1]);

                if (k < 1) {
                    System.out.println(
                            "error parsing " + s + ": trivially satisfied operator " + tok);
                    return null;
                } else if (k > n) {
                    System.out.println("error parsing " + s + ": unsatisfiable operator " + tok);
                    return null;
                } else if (n == 1) {
                    System.out.println("error parsing " + s + ": indentity operator " + tok);
                    return null;
                } else if (n > stack.size()) {
                    System.out.println("error parsing " + s + ": stack underflow at " + tok);
                    return null;
                }

                /* pop n things and fill in children */
                node = baseNode(k, null);
                node.children = new Policy[n];

                for (i = n - 1; i >= 0; i--)
                    node.children[i] = stack.remove(stack.size() - 1);

                /* push result */
                stack.add(node);
            }
        }

        if (stack.size() > 1) {
            System.out.println("error parsing " + s + ": extra node left on the stack");
            return null;
        } else if (stack.size() < 1) {
            System.out.println("error parsing " + s + ": empty policy");
            return null;
        }

        root = stack.get(0);
        return root;
    }

    private static Policy baseNode(int k, String s) {
        Policy p = new Policy();

        p.k = k;
        if (!(s == null))
            p.attr = s;
        else
            p.attr = null;
        p.q = null;

        return p;
    }

    private static void elementFromString(Element h, String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(s.getBytes());
        h.setFromHash(digest, 0, digest.length);
    }

}
