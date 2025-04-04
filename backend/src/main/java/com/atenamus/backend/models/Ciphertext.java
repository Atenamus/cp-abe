package com.atenamus.backend.models;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Ciphertext {
    private final byte[] aesEncryptedData; // AES-encrypted file bytes
    private final byte[] iv; // AES IV (16 bytes)
    private final Element C; // C = AES_key · e(g, g)^s
    private final Element C_prime; // C' = g^s
    private final List<CiphertextComponent> components; // {(C_i, D_i, E_i)}

    @Data
    @AllArgsConstructor
    public static class CiphertextComponent {
        private final Element C_i; // g^λ_i / (e(g, g)^α_θ · g^y_θ)^r_i
        private final Element D_i; // g^r_i
        private final Element E_i; // F(ρ(i))^r_i
        private final String attribute; // ρ(i)
    }
}