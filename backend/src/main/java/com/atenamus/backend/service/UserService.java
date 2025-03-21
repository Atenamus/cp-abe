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

    public ResponseEntity<UserPolicy> createPolicy(CreatePolicy policy, User user) {
        UserPolicy userPolicy = new UserPolicy();
        userPolicy.setPolicyName(policy.policyName);
        userPolicy.setPolicyDescription(policy.policyDescription);
        userPolicy.setPolicyExpression(policy.policyExpression);
        userPolicy.setUserId(user.getId());

        userPolicy = policyRepository.save(userPolicy);

        if (userPolicy.getId() != null) {
            log.info("Create policy successful");
            return new ResponseEntity<>(userPolicy, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> getPolicy(User user) {
        List<UserPolicy> policy = policyRepository.findByUserId(user.getId());

        if (!policy.isEmpty()) {
            return new ResponseEntity<>(policy, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No policy found", HttpStatus.NOT_FOUND);
        }
    }
}
