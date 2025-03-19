package com.atenamus.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    private String fullName;

    @ElementCollection
    @CollectionTable(name = "user_attributes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "attribute")
    private List<String> attributes;
}