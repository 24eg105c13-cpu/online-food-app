package com.example.foodordering.controller;

import com.example.foodordering.dto.AuthResponse;
import com.example.foodordering.dto.LoginRequest;
import com.example.foodordering.dto.RegisterRequest;
import com.example.foodordering.exception.DuplicateEmailException;
import com.example.foodordering.exception.GlobalExceptionHandler;
import com.example.foodordering.exception.UnauthorizedException;
import com.example.foodordering.model.Role;
import com.example.foodordering.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/auth/register: Valid request returns 201 Created and token")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Bob Williams")
                .email("bob@example.com")
                .password("Secret123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token-sample")
                .tokenType("Bearer")
                .id("u-1")
                .name("Bob Williams")
                .email("bob@example.com")
                .role(Role.CUSTOMER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-sample"))
                .andExpect(jsonPath("$.data.email").value("bob@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register: Validation failure (blank email, short password) returns 400 Bad Request")
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest badRequest = RegisterRequest.builder()
                .name("B") // Min 2 characters
                .email("invalid-email-format") // Invalid email
                .password("123") // Min 6 characters
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register: Duplicate email returns 409 Conflict")
    void testRegisterDuplicateEmailReturns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Bob Williams")
                .email("bob@example.com")
                .password("Secret123")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException("An account with email bob@example.com already exists."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An account with email bob@example.com already exists."));
    }

    @Test
    @DisplayName("POST /api/auth/login: Valid credentials return 200 OK and token")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("bob@example.com")
                .password("Secret123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-login-token")
                .name("Bob Williams")
                .email("bob@example.com")
                .role(Role.CUSTOMER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-login-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Invalid credentials return 401 Unauthorized")
    void testLoginInvalidCredentialsReturns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("bob@example.com")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
