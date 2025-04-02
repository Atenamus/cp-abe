package com.atenamus.backend.models;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorityPublicKey {
    private final Element e_g_g_alpha; // e(g, g)^α_θ
    private final Element g_y; // g^y_θ
    private final String authorityId;
}
