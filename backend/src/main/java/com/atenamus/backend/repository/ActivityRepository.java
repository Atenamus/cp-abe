package com.atenamus.backend.repository;

import com.atenamus.backend.models.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserIdOrderByTimestampDesc(Long userId);

    List<UserActivity> findTop8ByUserIdOrderByTimestampDesc(Long userId);
}