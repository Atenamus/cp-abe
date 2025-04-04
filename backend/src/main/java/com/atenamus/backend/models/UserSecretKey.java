package com.atenamus.backend.models;

import java.util.Map;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSecretKey {
    private final Element K_theta; // K_θ
    private final Element L; // L
    private final Map<String, Element> K_i; // {K_i}_{i∈S_θ}, attribute -> K_i
    private final String gid; // Global identifier
    private final String authorityId; // Authority identifier
}
