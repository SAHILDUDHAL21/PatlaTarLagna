package com.patlatarlagna.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patlatarlagna.dto.LoginRequest;
import com.patlatarlagna.dto.RegisterRequest;
import com.patlatarlagna.entity.Role;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.enums.RoleType;
import com.patlatarlagna.repository.RoleRepository;
import com.patlatarlagna.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController — full end-to-end REST API tests
 * using real H2 database, real security config, and real service layer.
 */
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Ensure roles exist
        if (roleRepository.findByName(RoleType.NORMAL_USER).isEmpty()) {
            roleRepository.save(Role.builder().name(RoleType.NORMAL_USER).build());
        }
        if (roleRepository.findByName(RoleType.ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().name(RoleType.ADMIN).build());
        }
    }

    // ========================================================================
    // REGISTER
    // ========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterIntegrationTests {

        @Test
        @DisplayName("Should register successfully with valid data")
        void register_success() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("integrationtest@example.com");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(containsString("Registration successful")));
        }

        @Test
        @DisplayName("Should return 400 for duplicate email")
        void register_duplicateEmail() throws Exception {
            Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElseThrow();
            User existing = User.builder()
                    .email("duplicate@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(true)
                    .roles(Set.of(normalRole))
                    .build();
            userRepository.saveAndFlush(existing);

            RegisterRequest req = new RegisterRequest();
            req.setEmail("duplicate@example.com");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("already registered")));
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void register_invalidEmail() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("not-an-email");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 for short password")
        void register_shortPassword() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("newuser@example.com");
            req.setPassword("ab");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 for blank email")
        void register_blankEmail() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================================================
    // LOGIN
    // ========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginIntegrationTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_success() throws Exception {
            Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElseThrow();
            User user = User.builder()
                    .email("logintest@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(true)
                    .roles(Set.of(normalRole))
                    .build();
            userRepository.saveAndFlush(user);

            LoginRequest req = new LoginRequest();
            req.setEmail("logintest@example.com");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value("logintest@example.com"))
                    .andExpect(jsonPath("$.data.roles").isArray())
                    .andExpect(jsonPath("$.data.roles", hasItem("ROLE_NORMAL_USER")));
        }

        @Test
        @DisplayName("Should return 400 for non-existent user")
        void login_userNotFound() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("ghost@example.com");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should return 400 for unverified account")
        void login_unverified() throws Exception {
            Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElseThrow();
            User user = User.builder()
                    .email("unverified@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(false)
                    .roles(Set.of(normalRole))
                    .build();
            userRepository.saveAndFlush(user);

            LoginRequest req = new LoginRequest();
            req.setEmail("unverified@example.com");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("not activated")));
        }
    }

    // ========================================================================
    // VERIFY EMAIL
    // ========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/verify-email")
    class VerifyEmailIntegrationTests {

        @Test
        @DisplayName("Should verify email with valid OTP")
        void verifyEmail_success() throws Exception {
            Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElseThrow();
            User user = User.builder()
                    .email("verify@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(false)
                    .verificationOtp("123456")
                    .verificationOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10))
                    .roles(Set.of(normalRole))
                    .build();
            userRepository.saveAndFlush(user);

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .param("email", "verify@example.com")
                            .param("otp", "123456"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(containsString("activated successfully")));
        }

        @Test
        @DisplayName("Should return 400 for invalid OTP")
        void verifyEmail_invalidOtp() throws Exception {
            Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElseThrow();
            User user = User.builder()
                    .email("verify2@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(false)
                    .verificationOtp("123456")
                    .verificationOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10))
                    .roles(Set.of(normalRole))
                    .build();
            userRepository.saveAndFlush(user);

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .param("email", "verify2@example.com")
                            .param("otp", "000000"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid verification OTP")));
        }
    }

    // ========================================================================
    // FORGOT PASSWORD
    // ========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    class ForgotPasswordIntegrationTests {

        @Test
        @DisplayName("Should send forgot-password OTP")
        void forgotPassword_success() throws Exception {
            Role normalRole = roleRepository.findByName(RoleType.NORMAL_USER).orElseThrow();
            User user = User.builder()
                    .email("forgot@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(true)
                    .roles(Set.of(normalRole))
                    .build();
            userRepository.saveAndFlush(user);

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"forgot@example.com\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(containsString("OTP has been sent")));
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void forgotPassword_notFound() throws Exception {
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"noone@example.com\"}"))
                    .andExpect(status().isNotFound());
        }
    }
}
