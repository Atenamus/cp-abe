package com.atenamus.backend.models;

import java.util.Set;
import java.util.function.Function;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GlobalParameters {
    private final int p; // Prime order
    private final Pairing pairing; // Bilinear group pairing
    private final Element gen; // Generator
    private final Field<Element> G; // Group G
    private final Set<String> attributeUniverse; // U
    private final Set<String> authorityUniverse; // U_Θ
    private final Function<String, Element> hFunction; // H: GID -> G
    private final Function<String, Element> fFunction; // F: attributes -> G

    public Element applyHFunction(String gid) {
        return hFunction.apply(gid).duplicate();
    }

    public Element applyFFunction(String attribute) {
        return fFunction.apply(attribute).duplicate();
    }
}
