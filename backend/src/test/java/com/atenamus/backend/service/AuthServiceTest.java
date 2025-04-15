package com.atenamus.backend.service;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.dto.SignInRequest;
import com.atenamus.backend.dto.SignUpRequest;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.models.User;
import com.atenamus.backend.repository.UserRepository;
import com.atenamus.backend.security.JwtUtil;
import com.atenamus.backend.util.AttributeUtil;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Cpabe cpabe;

    @InjectMocks
    private AuthService authService;

    private SignUpRequest singUpRequest;
    private SignInRequest signInRequest;
    private User user;
    private final String email = "test@atenamus.com";
    private final String password = "password";
    private final String encodedPassword = "encodedPassword";
    private final String token = "jwtGeneratedToken";
    private final List<String> attributes = Arrays.asList("role:admin", "department:IT");

    @BeforeEach
    void setup() {
        singUpRequest = new SignUpRequest();
        singUpRequest.setEmail(email);
        singUpRequest.setPassword(password);
        String fullName = "fullName";
        singUpRequest.setFullName(fullName);
        singUpRequest.setAttributes(attributes);

        user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setFullName(fullName);
        user.setAttributes(attributes);

        signInRequest = new SignInRequest();
        signInRequest.setEmail(email);
        signInRequest.setPassword(password);
    }

    @Test
    void signUpSuccessTest() throws Exception {
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(jwtUtil.generateToken(email)).thenReturn(token);

        try (MockedStatic<AttributeUtil> attributeUtilMockedStatic = Mockito.mockStatic(AttributeUtil.class);
             MockedStatic<FileUtil> fileUtilMockedStatic = Mockito.mockStatic(FileUtil.class);
             MockedStatic<SerializeUtil> serializeUtilMockedStatic = Mockito.mockStatic(SerializeUtil.class)
        ) {
            attributeUtilMockedStatic.when(() -> AttributeUtil.validateAttribute(anyString())).thenReturn(true);

            byte[] mockPubBytes = new byte[]{1, 2, 3};
            byte[] mockMskBytes = new byte[]{4, 5, 6};
            byte[] mockPrvBytes = new byte[]{7, 8, 9};

            PublicKey mockPub = mock(PublicKey.class);
            MasterSecretKey mockMsk = mock(MasterSecretKey.class);
            PrivateKey mockPrv = mock(PrivateKey.class);

            fileUtilMockedStatic.when(() -> FileUtil.readFile("public_key.dat")).thenReturn(mockPubBytes);
            fileUtilMockedStatic.when(() -> FileUtil.readFile("master_secret_key.dat")).thenReturn(mockMskBytes);

            serializeUtilMockedStatic.when(() -> SerializeUtil.unserializePublicKey(mockPubBytes)).thenReturn(mockPub);
            serializeUtilMockedStatic.when(() -> SerializeUtil.unserializeMasterSecretKey(mockPub, mockMskBytes)).thenReturn(mockMsk);
            serializeUtilMockedStatic.when(() -> SerializeUtil.serializePrivateKey(any())).thenReturn(mockPrvBytes);

            when(cpabe.keygen(any(), any(), any())).thenReturn(mockPrv);

            String result = authService.signup(singUpRequest);

            assertEquals(token, result);
            verify(userRepository).save(any(User.class));
            fileUtilMockedStatic.verify(() -> FileUtil.writeFile("private_key.dat", mockPrvBytes));
        }
    }

    @Test
    void signupEmailExistsTest() {
        when(userRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> authService.signup(singUpRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signupCpabeErrorTest() {
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        try (MockedStatic<AttributeUtil> attributeUtilMock = mockStatic(AttributeUtil.class);
             MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {

            attributeUtilMock.when(() -> AttributeUtil.validateAttributes(any())).thenReturn(attributes);

            fileUtilMock.when(() -> FileUtil.readFile(anyString()))
                    .thenThrow(new RuntimeException("File read error"));

            Exception exception = assertThrows(RuntimeException.class, () -> authService.signup(singUpRequest));

            assertTrue(exception.getMessage().contains("Error generating CP-ABE key"));
            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void signinSuccessTest() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email)).thenReturn(token);

        String result = authService.signin(signInRequest);

        assertEquals(token, result);
    }

    @Test
    void signinUserNotFoundTest() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> authService.signin(signInRequest));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void signinInvalidPasswordTest() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> authService.signin(signInRequest));

        assertEquals("Invalid password", exception.getMessage());
    }

}
