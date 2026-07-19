package com.patlatarlagna.service;

import com.patlatarlagna.dto.*;
import com.patlatarlagna.entity.RefreshToken;
import com.patlatarlagna.entity.Role;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.enums.RoleType;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.repository.ProfileRepository;
import com.patlatarlagna.repository.RoleRepository;
import com.patlatarlagna.repository.UserRepository;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.security.jwt.JwtTokenProvider;
import com.patlatarlagna.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl — core authentication, registration,
 * email verification, password management, and token refresh logic.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder encoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role normalUserRole;
    private User existingUser;

    @BeforeEach
    void setUp() {
        normalUserRole = Role.builder().id(1L).name(RoleType.NORMAL_USER).build();

        existingUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .roles(Set.of(normalUserRole))
                .build();
    }

    // ========================================================================
    // REGISTER
    // ========================================================================
    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_success() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("new@example.com");
            req.setPassword("password123");

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(roleRepository.findByName(RoleType.NORMAL_USER)).thenReturn(Optional.of(normalUserRole));
            when(encoder.encode("password123")).thenReturn("encodedPass");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.register(req);

            verify(userRepository).save(argThat(user -> {
                assertThat(user.getEmail()).isEqualTo("new@example.com");
                assertThat(user.isEnabled()).isFalse();
                assertThat(user.getVerificationOtp()).isNotNull();
                assertThat(user.getVerificationOtp()).hasSize(6);
                return true;
            }));
            verify(emailService).sendOtpEmail(eq("new@example.com"), anyString());
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void register_duplicateEmail() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@example.com");
            req.setPassword("password123");

            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("Should throw when default role is not found")
        void register_noDefaultRole() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("new@example.com");
            req.setPassword("password123");

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(roleRepository.findByName(RoleType.NORMAL_USER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Default User Role not found");
        }
    }

    // ========================================================================
    // LOGIN
    // ========================================================================
    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_success() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("password123");

            CustomUserDetails userDetails = new CustomUserDetails(
                    1L, "test@example.com", "encodedPassword", true,
                    List.of(new SimpleGrantedAuthority("ROLE_NORMAL_USER"))
            );

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(userDetails);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(jwtTokenProvider.generateJwtToken(auth)).thenReturn("jwt-token");
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("refresh-token")
                    .user(existingUser)
                    .expiryDate(Instant.now().plusMillis(86400000))
                    .build();
            when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

            JwtResponse response = authService.login(req);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRoles()).containsExactly("ROLE_NORMAL_USER");
            assertThat(response.isHasProfile()).isFalse();
        }

        @Test
        @DisplayName("Should throw when user not found")
        void login_userNotFound() {
            LoginRequest req = new LoginRequest();
            req.setEmail("nonexistent@example.com");
            req.setPassword("password123");

            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw when account not verified")
        void login_notVerified() {
            LoginRequest req = new LoginRequest();
            req.setEmail("test@example.com");
            req.setPassword("password123");

            User disabledUser = User.builder()
                    .id(2L)
                    .email("test@example.com")
                    .password("encodedPassword")
                    .enabled(false)
                    .roles(Set.of(normalUserRole))
                    .build();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(disabledUser));

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not activated");
        }
    }

    // ========================================================================
    // VERIFY EMAIL
    // ========================================================================
    @Nested
    @DisplayName("verifyEmail()")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify email with valid OTP")
        void verifyEmail_success() {
            User unverified = User.builder()
                    .id(3L)
                    .email("unverified@example.com")
                    .password("encoded")
                    .enabled(false)
                    .verificationOtp("123456")
                    .verificationOtpExpiry(LocalDateTime.now().plusMinutes(5))
                    .build();

            when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(unverified));

            authService.verifyEmail("unverified@example.com", "123456");

            assertThat(unverified.isEnabled()).isTrue();
            assertThat(unverified.getVerificationOtp()).isNull();
            assertThat(unverified.getVerificationOtpExpiry()).isNull();
            verify(userRepository).save(unverified);
        }

        @Test
        @DisplayName("Should throw when account already verified")
        void verifyEmail_alreadyVerified() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> authService.verifyEmail("test@example.com", "123456"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already verified");
        }

        @Test
        @DisplayName("Should throw when OTP is invalid")
        void verifyEmail_invalidOtp() {
            User unverified = User.builder()
                    .id(3L)
                    .email("unverified@example.com")
                    .enabled(false)
                    .verificationOtp("654321")
                    .verificationOtpExpiry(LocalDateTime.now().plusMinutes(5))
                    .build();

            when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(unverified));

            assertThatThrownBy(() -> authService.verifyEmail("unverified@example.com", "123456"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid verification OTP");
        }

        @Test
        @DisplayName("Should throw when OTP is expired")
        void verifyEmail_expiredOtp() {
            User unverified = User.builder()
                    .id(3L)
                    .email("unverified@example.com")
                    .enabled(false)
                    .verificationOtp("123456")
                    .verificationOtpExpiry(LocalDateTime.now().minusMinutes(1)) // expired
                    .build();

            when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(unverified));

            assertThatThrownBy(() -> authService.verifyEmail("unverified@example.com", "123456"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expired");
        }
    }

    // ========================================================================
    // FORGOT / RESET PASSWORD
    // ========================================================================
    @Nested
    @DisplayName("forgotPassword() / resetPassword()")
    class PasswordResetTests {

        @Test
        @DisplayName("Should send reset OTP on forgotPassword")
        void forgotPassword_success() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

            authService.forgotPassword("test@example.com");

            assertThat(existingUser.getResetOtp()).isNotNull().hasSize(6);
            assertThat(existingUser.getResetOtpExpiry()).isAfter(LocalDateTime.now());
            verify(userRepository).save(existingUser);
            verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
        }

        @Test
        @DisplayName("Should throw on forgotPassword when user not found")
        void forgotPassword_notFound() {
            when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.forgotPassword("missing@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should reset password with valid OTP")
        void resetPassword_success() {
            existingUser.setResetOtp("111111");
            existingUser.setResetOtpExpiry(LocalDateTime.now().plusMinutes(5));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setEmail("test@example.com");
            req.setOtp("111111");
            req.setNewPassword("newPassword123");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
            when(encoder.encode("newPassword123")).thenReturn("encodedNewPass");

            authService.resetPassword(req);

            assertThat(existingUser.getPassword()).isEqualTo("encodedNewPass");
            assertThat(existingUser.getResetOtp()).isNull();
            assertThat(existingUser.getResetOtpExpiry()).isNull();
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw on resetPassword with invalid OTP")
        void resetPassword_invalidOtp() {
            existingUser.setResetOtp("111111");
            existingUser.setResetOtpExpiry(LocalDateTime.now().plusMinutes(5));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setEmail("test@example.com");
            req.setOtp("999999");
            req.setNewPassword("newPassword123");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> authService.resetPassword(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid reset OTP");
        }

        @Test
        @DisplayName("Should throw on resetPassword with expired OTP")
        void resetPassword_expiredOtp() {
            existingUser.setResetOtp("111111");
            existingUser.setResetOtpExpiry(LocalDateTime.now().minusMinutes(1)); // expired

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setEmail("test@example.com");
            req.setOtp("111111");
            req.setNewPassword("newPassword123");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> authService.resetPassword(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expired");
        }
    }

    // ========================================================================
    // CHANGE PASSWORD
    // ========================================================================
    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password when old password matches")
        void changePassword_success() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("oldPassword");
            req.setNewPassword("newPassword123");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
            when(encoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
            when(encoder.encode("newPassword123")).thenReturn("encodedNewPass");

            authService.changePassword("test@example.com", req);

            assertThat(existingUser.getPassword()).isEqualTo("encodedNewPass");
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should throw when old password is incorrect")
        void changePassword_wrongOldPassword() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setOldPassword("wrongPassword");
            req.setNewPassword("newPassword123");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
            when(encoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword("test@example.com", req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Incorrect old password");
        }
    }

    // ========================================================================
    // REFRESH TOKEN
    // ========================================================================
    @Nested
    @DisplayName("refreshJwtToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh JWT token with valid refresh token")
        void refreshToken_success() {
            TokenRefreshRequest req = new TokenRefreshRequest();
            req.setRefreshToken("valid-refresh-token");

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("valid-refresh-token")
                    .user(existingUser)
                    .expiryDate(Instant.now().plusMillis(86400000))
                    .build();

            when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
            when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
            when(jwtTokenProvider.generateTokenFromUsername("test@example.com")).thenReturn("new-jwt-token");
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

            JwtResponse response = authService.refreshJwtToken(req);

            assertThat(response.getToken()).isEqualTo("new-jwt-token");
            assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw when refresh token not found")
        void refreshToken_notFound() {
            TokenRefreshRequest req = new TokenRefreshRequest();
            req.setRefreshToken("invalid-refresh-token");

            when(refreshTokenService.findByToken("invalid-refresh-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshJwtToken(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not in database");
        }
    }
}
