package com.atenamus.backend.service;

import com.atenamus.backend.dto.SignInRequest;
import com.atenamus.backend.dto.SignUpRequest;
import com.atenamus.backend.repository.UserRepository;
import com.atenamus.backend.security.JwtUtil;
import com.atenamus.backend.Cpabe;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.models.User;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.util.SerializeUtil;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.AttributeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Cpabe cpabe;

    private static final String PUB_KEY_FILE = "public_key.dat";
    private static final String MSK_KEY_FILE = "master_secret_key.dat";
    private static final String PRV_KEY_FILE = "private_key.dat";

    public String signup(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate attributes format
        List<String> validatedAttributes =
                AttributeUtil.validateAttributes(request.getAttributes());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setAttributes(validatedAttributes);

        userRepository.save(user);

        // Generate CP-ABE private key for user
        try {
            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            byte[] mskBytes = FileUtil.readFile(MSK_KEY_FILE);
            MasterSecretKey msk = SerializeUtil.unserializeMasterSecretKey(pub, mskBytes);

            String[] attributesArray = validatedAttributes.toArray(new String[0]);
            PrivateKey prv = cpabe.keygen(pub, msk, attributesArray);

            // Add traceability information
            prv.userId = user.getId().toString();
            prv.userEmail = user.getEmail();
            prv.timestamp = System.currentTimeMillis();
            // Set expiration to 1 year from now
            prv.expirationDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000);

            byte[] prvBytes = SerializeUtil.serializePrivateKey(prv);
            FileUtil.writeFile(PRV_KEY_FILE, prvBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating CP-ABE key: " + e.getMessage());
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    public String signin(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(request.getEmail());
    }
}
