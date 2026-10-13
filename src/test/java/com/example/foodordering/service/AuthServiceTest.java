package com.example.foodordering.service;

import com.example.foodordering.dto.AuthResponse;
import com.example.foodordering.dto.LoginRequest;
import com.example.foodordering.dto.RegisterRequest;
import com.example.foodordering.exception.DuplicateEmailException;
import com.example.foodordering.exception.UnauthorizedException;
import com.example.foodordering.model.Role;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.UserRepository;
import com.example.foodordering.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User customerUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Alice Smith")
                .email("alice@example.com")
                .password("Password123")
                .phone("1234567890")
                .address("123 Main Street")
                .build();

        loginRequest = LoginRequest.builder()
                .email("alice@example.com")
                .password("Password123")
                .build();

        customerUser = User.builder()
                .id("user-123")
                .name("Alice Smith")
                .email("alice@example.com")
                .passwordHash("hashedPassword123")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("Register: Successfully registers new customer and returns JWT token")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(customerUser);
        when(jwtUtil.generateToken(customerUser.getId(), customerUser.getEmail(), Role.CUSTOMER))
                .thenReturn("mock-jwt-token-xyz");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token-xyz", response.getToken());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());

        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.CUSTOMER &&
                user.getPasswordHash().equals("hashedPassword123") &&
                user.getEmail().equals("alice@example.com")
        ));
    }

    @Test
    @DisplayName("Register: Throws DuplicateEmailException when email is already registered")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login: Successfully authenticates valid credentials")
    void testLoginSuccess() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(customerUser));
        when(passwordEncoder.matches("Password123", "hashedPassword123")).thenReturn(true);
        when(jwtUtil.generateToken(customerUser.getId(), customerUser.getEmail(), Role.CUSTOMER))
                .thenReturn("mock-jwt-token-xyz");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token-xyz", response.getToken());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("Alice Smith", response.getName());
    }

    @Test
    @DisplayName("Login: Throws UnauthorizedException when password does not match")
    void testLoginInvalidPassword() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(customerUser));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword123")).thenReturn(false);

        LoginRequest badRequest = LoginRequest.builder()
                .email("alice@example.com")
                .password("WrongPassword")
                .build();

        assertThrows(UnauthorizedException.class, () -> authService.login(badRequest));
    }

    @Test
    @DisplayName("Login: Throws UnauthorizedException when email does not exist")
    void testLoginUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest unknownRequest = LoginRequest.builder()
                .email("unknown@example.com")
                .password("Password123")
                .build();

        assertThrows(UnauthorizedException.class, () -> authService.login(unknownRequest));
    }
}
