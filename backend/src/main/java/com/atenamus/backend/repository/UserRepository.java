package com.atenamus.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atenamus.backend.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}