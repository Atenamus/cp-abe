package com.atenamus.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SignUpRequest {
    private String email;
    private String password;
    private String fullName;
    private List<String> attributes;
}