package com.atenamus.backend;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.atenamus.backend.models.Cipher;
import com.atenamus.backend.models.CipherKey;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.Policy;
import com.atenamus.backend.models.Polynomial;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PrivateKeyComp;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.util.PolicyParser;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class Cpabe {

    // Predefined elliptic curve parameters for the bilinear pairing
    private static final String curveParams = "type a\n"
            + "q 87807107996633125224377819847540498158068831994142082"
            + "1102865339926647563088022295707862517942266222142315585"
            + "8769582317459277713367317481324925129998224791\n"
            + "h 12016012264891146079388821366740534204802954401251311"
            + "822919615131047207289359704531102844802183906537786776\n"
            + "r 730750818665451621361119245571504901405976559617\n" + "exp2 159\n"
            + "exp1 107\n" + "sign1 1\n" + "sign0 1\n";

    /**
     * Setup the CP-ABE system by generating public and master secret keys.
     *
     * @param pub The public key to be generated.
     * @param msk The master secret key to be generated.
     */
    public void setup(PublicKey pub, MasterSecretKey msk) {
        Element alpha, betaInverse;

        // Load pairing parameters from the predefined curve parameters
        PropertiesParameters params = new PropertiesParameters();
        params.load(new ByteArrayInputStream(curveParams.getBytes()));

        // Initialize the pairing (bilinear map)
        pub.pairingDesc = curveParams;
        pub.p = PairingFactory.getPairing(params);
        Pairing pairing = pub.p;

        // Initialize public key elements
        pub.g = pairing.getG1().newElement(); // Generator of group G1
        pub.f = pairing.getG1().newElement(); // f = g^(1/beta)
        pub.h = pairing.getG1().newElement(); // h = g^beta
        pub.gp = pairing.getG2().newElement(); // Generator of group G2
        pub.g_hat_alpha = pairing.getGT().newElement(); // e(g, g)^alpha

        // Initialize master secret key elements
        alpha = pairing.getZr().newElement(); // Random alpha
        msk.beta = pairing.getZr().newElement(); // Random beta
        msk.g_alpha = pairing.getG2().newElement(); // g^alpha

        // Set random values for alpha and beta
        alpha.setToRandom();
        msk.beta.setToRandom();
        pub.g.setToRandom();
        pub.gp.setToRandom();

        // Compute g_alpha = gp^alpha
        msk.g_alpha = pub.gp.duplicate();
        msk.g_alpha.powZn(alpha);

        // Compute beta inverse
        betaInverse = msk.beta.duplicate();
        betaInverse.invert();

        // Compute f = g^(1/beta)
        pub.f = pub.g.duplicate();
        pub.f.powZn(betaInverse);

        // Compute h = g^beta
        pub.h = pub.g.duplicate();
        pub.h.powZn(msk.beta);

        // Compute e(g, g_alpha) (g_hat_alpha)
        pub.g_hat_alpha = pairing.pairing(pub.g, msk.g_alpha);
    }

    /**
     * Generate a private key for a user with a given set of attributes.
     *
     * @param pub   The public key.
     * @param msk   The master secret key.
     * @param attrs The attributes associated with the user.
     * @return The generated private key.
     * @throws NoSuchAlgorithmException If the hash algorithm is not available.
     */
    public PrivateKey keygen(PublicKey pub, MasterSecretKey msk, String[] attrs)
            throws NoSuchAlgorithmException {
        PrivateKey privateKey = new PrivateKey();
        Element g_r, r, betaInverse;
        Pairing pairing;

        /* Initialize */
        pairing = pub.p;
        privateKey.d = pairing.getG2().newElement(); // D = g^((alpha + r)/beta)
        g_r = pairing.getG2().newElement(); // g^r
        r = pairing.getZr().newElement(); // Random r
        betaInverse = pairing.getZr().newElement(); // 1/beta

        /* Compute */
        r.setToRandom(); // Randomize r
        g_r = pub.gp.duplicate();
        g_r.powZn(r); // g_r = g^r

        privateKey.d = msk.g_alpha.duplicate(); // D = g^alpha
        privateKey.d.mul(g_r); // D = g^(alpha + r)
        betaInverse = msk.beta.duplicate();
        betaInverse.invert(); // betaInverse = 1/beta
        privateKey.d.powZn(betaInverse); // D = g^((alpha + r)/beta)

        // Generate private key components for each attribute
        int i, len = attrs.length;
        privateKey.comps = new ArrayList<PrivateKeyComp>();
        for (i = 0; i < len; i++) {
            PrivateKeyComp comp = new PrivateKeyComp();
            Element h_rp;
            Element rp;

            comp.attr = attrs[i]; // Attribute name

            comp.d = pairing.getG2().newElement(); // D_j = g^r * H(j)^r_j
            comp.dp = pairing.getG1().newElement(); // D'_j = g^r_j
            h_rp = pairing.getG2().newElement(); // H(j)^r_j
            rp = pairing.getZr().newElement(); // Random r_j

            elementFromString(h_rp, comp.attr); // H(j) = hash of attribute
            rp.setToRandom(); // Randomize r_j

            h_rp.powZn(rp); // H(j)^r_j

            comp.d = g_r.duplicate(); // D_j = g^r
            comp.d.mul(h_rp); // D_j = g^r * H(j)^r_j
            comp.dp = pub.g.duplicate(); // D'_j = g^r_j
            comp.dp.powZn(rp);

            privateKey.comps.add(comp);
        }

        return privateKey;
    }

    /**
     * Encrypt a message under the specified policy using CP-ABE.
     * This method generates a random symmetric key and encrypts it using the CP-ABE
     * scheme.
     * The actual message should be encrypted separately using the returned
     * symmetric key.
     *
     * @param pub    The public key.
     * @param policy The access policy string (e.g., "admin AND (finance OR hr)").
     * @return A key-ciphertext pair containing the CP-ABE ciphertext and symmetric
     *         key.
     * @throws Exception If policy parsing or encryption operations fail.
     */
    public CipherKey encrypt(PublicKey pub, String policy) throws Exception {
        CipherKey cipherKey = new CipherKey();
        Cipher cipher = new Cipher();
        Element secretValue, symmetricKey;

        /* Initialize */
        Pairing pairing = pub.p;
        secretValue = pairing.getZr().newElement(); // Random secret value s
        symmetricKey = pairing.getGT().newElement(); // Random symmetric key m
        cipher.cs = pairing.getGT().newElement(); // Ciphertext component C~
        cipher.c = pairing.getG1().newElement(); // Ciphertext component C

        // Parse policy into a tree
        String postfixPolicy = PolicyParser.convertInfixToPostfix(policy);
        System.out.println("Postfix policy: " + postfixPolicy);
        cipher.p = parsePolicyPostfix(postfixPolicy);

        /* Compute */
        symmetricKey.setToRandom(); // Randomize symmetric key
        secretValue.setToRandom(); // Randomize secret value s
        cipher.cs = pub.g_hat_alpha.duplicate(); // C~ = e(g, g)^alpha
        cipher.cs.powZn(secretValue); // C~ = e(g, g)^(alpha * s)
        cipher.cs.mul(symmetricKey); // C~ = m * e(g, g)^(alpha * s)

        cipher.c = pub.h.duplicate(); // C = h
        cipher.c.powZn(secretValue); // C = h^s

        // Fill the policy tree with shares of the secret value
        fillPolicy(cipher.p, pub, secretValue);

        cipherKey.cph = cipher;
        cipherKey.key = symmetricKey;

        return cipherKey;
    }

    /**
     * Recursively fill the policy tree with shares of the secret value.
     *
     * @param policyNode  The current node in the policy tree.
     * @param pub         The public key.
     * @param secretShare The secret share to be assigned to the node.
     * @throws NoSuchAlgorithmException If the hash algorithm is not available.
     */
    private static void fillPolicy(Policy policyNode, PublicKey pub, Element secretShare)
            throws NoSuchAlgorithmException {
        int i;
        Element index, shareValue, hashValue;
        Pairing pairing = pub.p;
        index = pairing.getZr().newElement(); // Index for polynomial evaluation
        shareValue = pairing.getZr().newElement(); // Share value for child nodes
        hashValue = pairing.getG2().newElement(); // Hash of attribute

        // Generate a polynomial for the current node
        policyNode.q = generatePolynomial(policyNode.k - 1, secretShare);

        if (policyNode.children == null || policyNode.children.length == 0) {
            // Leaf node: compute ciphertext components
            policyNode.c = pairing.getG1().newElement(); // C_y = g^q(0)
            policyNode.cp = pairing.getG2().newElement(); // C'_y = H(attr)^q(0)

            elementFromString(hashValue, policyNode.attr); // H(attr)
            policyNode.c = pub.g.duplicate();
            policyNode.c.powZn(policyNode.q.coef[0]); // C_y = g^q(0)
            policyNode.cp = hashValue.duplicate();
            policyNode.cp.powZn(policyNode.q.coef[0]); // C'_y = H(attr)^q(0)
        } else {
            // Internal node: recursively assign shares to child nodes
            for (i = 0; i < policyNode.children.length; i++) {
                index.set(i + 1); // Set index for child node
                evaluatePolynomial(shareValue, policyNode.q, index); // Compute share for child
                fillPolicy(policyNode.children[i], pub, shareValue); // Recursively fill child
            }
        }
    }

    /**
     * Evaluate a polynomial at a given point.
     *
     * @param result The result of the evaluation.
     * @param poly   The polynomial to evaluate.
     * @param x      The point at which to evaluate the polynomial.
     */
    private static void evaluatePolynomial(Element result, Polynomial poly, Element x) {
        int i;
        Element term, power;

        term = result.duplicate();
        power = result.duplicate();

        result.setToZero(); // Initialize result to 0
        power.setToOne(); // Initialize power to 1

        for (i = 0; i < poly.deg + 1; i++) {
            // result += poly.coef[i] * power
            term = poly.coef[i].duplicate();
            term.mul(power);
            result.add(term);
            // power *= x
            power.mul(x);
        }
    }

    /**
     * Generate a random polynomial of a given degree.
     *
     * @param degree       The degree of the polynomial.
     * @param constantTerm The constant term of the polynomial.
     * @return The generated polynomial.
     */
    private static Polynomial generatePolynomial(int degree, Element constantTerm) {
        int i;
        Polynomial poly = new Polynomial();
        poly.deg = degree;
        poly.coef = new Element[degree + 1];

        for (i = 0; i < degree + 1; i++)
            poly.coef[i] = constantTerm.duplicate();
        poly.coef[0].set(constantTerm); // Set constant term

        for (i = 1; i < degree + 1; i++)
            poly.coef[i].setToRandom(); // Randomize other coefficients

        return poly;
    }

    /**
     * Parse a policy string into a policy tree.
     *
     * @param policyString The policy string to parse.
     * @return The root of the policy tree.
     * @throws Exception If parsing fails.
     */
    private static Policy parsePolicyPostfix(String policyString) throws Exception {
        String[] tokens = policyString.split(" ");
        ArrayList<Policy> stack = new ArrayList<>();
        Policy root;

        for (String token : tokens) {
            if (!token.contains("of") && !token.matches("\\d+")) {
                // Leaf node: push to stack
                stack.add(createPolicyNode(1, token));
            } else if (token.contains("of")) {
                // Handle k-of-n nodes
                String[] k_n = token.split("of");
                int k = Integer.parseInt(k_n[0]);
                int n = Integer.parseInt(k_n[1]);

                if (k < 1 || k > n || n == 1 || n > stack.size()) {
                    throw new Exception("Invalid k-of-n operator: " + token);
                }

                // Pop n nodes from the stack and create a new k-of-n node
                Policy node = createPolicyNode(k, null);
                node.children = new Policy[n];
                for (int i = n - 1; i >= 0; i--) {
                    node.children[i] = stack.remove(stack.size() - 1);
                }

                // Push the new node to the stack
                stack.add(node);
            } else {
                throw new Exception("Invalid token: " + token);
            }
        }

        if (stack.size() != 1) {
            throw new Exception("Invalid policy: stack contains multiple or no nodes");
        }

        root = stack.get(0);
        return root;
    }

    /**
     * Create a new policy node.
     *
     * @param threshold The threshold value for the node.
     * @param attribute The attribute associated with the node (if any).
     * @return The created policy node.
     */
    private static Policy createPolicyNode(int threshold, String attribute) {
        Policy node = new Policy();

        node.k = threshold; // Set threshold
        if (!(attribute == null))
            node.attr = attribute; // Set attribute
        else
            node.attr = null;
        node.q = null; // Polynomial will be generated later

        return node;
    }

    /**
     * Convert a string to a group element using a hash function.
     *
     * @param element The group element to set.
     * @param str     The string to hash.
     * @throws NoSuchAlgorithmException If the hash algorithm is not available.
     */
    private static void elementFromString(Element element, String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(str.getBytes());
        element.setFromHash(digest, 0, digest.length);
    }
}