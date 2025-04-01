package com.atenamus.backend.repository;

import com.atenamus.backend.models.UserPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<UserPolicy,Long> {
    List<UserPolicy> findByUserId(Long id);
}
