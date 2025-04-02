package com.atenamus.backend.util;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.crypto.digests.SHA256Digest;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

public class RandomOracle {
    private final Field<Element> field;
    private final SHA256Digest digest;

    public RandomOracle(Field<Element> field) {
        this.field = field;
        this.digest = new SHA256Digest();
    }

    public Element hashToElement(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input to hash cannot be null or empty");
        }

        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.update(inputBytes, 0, inputBytes.length);
        digest.doFinal(hash, 0);

        // Map hash to field element and ensure it's valid
        Element element = field.newElementFromBytes(hash);
        if (element.isZero()) {
            // If zero, perturb the input and try again
            digest.update("perturb".getBytes(StandardCharsets.UTF_8), 0, 7);
            digest.doFinal(hash, 0);
            element = field.newElementFromBytes(hash);
        }
        return element.getImmutable();
    }
}
