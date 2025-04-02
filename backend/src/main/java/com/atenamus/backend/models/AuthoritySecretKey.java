package com.atenamus.backend.models;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthoritySecretKey {
    private final Element alpha; // α_θ
    private final Element y; // y_θ
    private final String authorityId;
}
