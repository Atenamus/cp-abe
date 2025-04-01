package com.atenamus.backend.service;

import com.atenamus.backend.dto.CreatePolicy;
import com.atenamus.backend.models.User;
import com.atenamus.backend.models.UserPolicy;
import com.atenamus.backend.repository.PolicyRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ActivityService activityService;

    public ResponseEntity<UserPolicy> createPolicy(CreatePolicy policy, User user) {
        UserPolicy userPolicy = new UserPolicy();
        userPolicy.setPolicyName(policy.policyName);
        userPolicy.setPolicyDescription(policy.policyDescription);
        userPolicy.setPolicyExpression(policy.policyExpression);
        userPolicy.setUserId(user.getId());

        userPolicy = policyRepository.save(userPolicy);

        if (userPolicy.getId() != null) {
            // Track policy creation activity
            activityService.trackActivity(
                    user.getId(),
                    "policy_created",
                    policy.policyName,
                    "Policy created: " + policy.policyExpression);
            log.info("Create policy successful");
            return new ResponseEntity<>(userPolicy, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> getPolicy(User user) {
        List<UserPolicy> policies = policyRepository.findByUserId(user.getId());
        return new ResponseEntity<>(policies, policies.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    public ResponseEntity<?> deletePolicy(Long id, User user) {
        Optional<UserPolicy> policy = policyRepository.findById(id);

        if (policy.isPresent() && policy.get().getUserId().equals(user.getId())) {
            UserPolicy policyToDelete = policy.get();
            policyRepository.deleteById(id);

            // Track policy deletion activity
            activityService.trackActivity(
                    user.getId(),
                    "policy_deleted",
                    policyToDelete.getPolicyName(),
                    "Policy deleted");
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Policy not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> updatePolicy(Long id, CreatePolicy request, User user) {
        Optional<UserPolicy> policy = policyRepository.findById(id);

        if (policy.isPresent() && policy.get().getUserId().equals(user.getId())) {
            UserPolicy userPolicy = policy.get();
            userPolicy.setPolicyName(request.policyName);
            userPolicy.setPolicyDescription(request.policyDescription);
            userPolicy.setPolicyExpression(request.policyExpression);

            userPolicy = policyRepository.save(userPolicy);

            // Track policy update activity
            activityService.trackActivity(
                    user.getId(),
                    "policy_updated",
                    request.policyName,
                    "Policy updated: " + request.policyExpression);
            return new ResponseEntity<>(userPolicy, HttpStatus.OK);
        }
        return new ResponseEntity<>("Policy not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getPolicyById(Long id, User user) {
        Optional<UserPolicy> policy = policyRepository.findById(id);

        if (policy.isPresent() && policy.get().getUserId().equals(user.getId())) {
            return new ResponseEntity<>(policy.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Policy not found", HttpStatus.NOT_FOUND);
    }
}
