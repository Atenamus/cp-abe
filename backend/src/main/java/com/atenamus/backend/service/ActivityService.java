package com.atenamus.backend.service;

import com.atenamus.backend.models.UserActivity;
import com.atenamus.backend.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class ActivityService {
    @Autowired
    private ActivityRepository activityRepository;

    public void trackActivity(Long userId, String type, String resourceName, String details) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setType(type);
        activity.setResourceName(resourceName);
        activity.setDetails(details);
        activity.setTimestamp(Instant.now());
        activityRepository.save(activity);
    }

    public List<UserActivity> getRecentActivities(Long userId) {
        return activityRepository.findTop8ByUserIdOrderByTimestampDesc(userId);
    }

    public List<UserActivity> getAllActivities(Long userId) {
        return activityRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}