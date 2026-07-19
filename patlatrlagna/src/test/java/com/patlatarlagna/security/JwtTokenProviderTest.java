package com.patlatarlagna.security;

import com.patlatarlagna.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider — token generation, validation, and
 * username extraction from JWT tokens.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // A valid 512-bit Base64-encoded secret for HS512
    private static final String TEST_SECRET =
            "9a6564c126a11d027e85c15e8c33878b6689d023f03b879124a686b24bc9ef8f" +
            "3e2b1049ffcd5a890a2a4e21a084ef701cdbeab3216892e62a048a6b185675c9";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000); // 1 hour
    }

    @Test
    @DisplayName("Should generate and validate JWT token from Authentication")
    void generateAndValidateToken() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "test@example.com", "password", true,
                List.of(new SimpleGrantedAuthority("ROLE_NORMAL_USER"))
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String token = jwtTokenProvider.generateJwtToken(auth);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtTokenProvider.validateJwtToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void extractUsername() {
        String token = jwtTokenProvider.generateTokenFromUsername("test@example.com");

        String username = jwtTokenProvider.getUserNameFromJwtToken(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should generate token from username string")
    void generateFromUsername() {
        String token = jwtTokenProvider.generateTokenFromUsername("user@domain.com");

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtTokenProvider.validateJwtToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserNameFromJwtToken(token)).isEqualTo("user@domain.com");
    }

    @Test
    @DisplayName("Should return false for invalid JWT token")
    void validateInvalidToken() {
        assertThat(jwtTokenProvider.validateJwtToken("totally.not.a.jwt.token")).isFalse();
    }

    @Test
    @DisplayName("Should return false for null token")
    void validateNullToken() {
        assertThat(jwtTokenProvider.validateJwtToken(null)).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty token")
    void validateEmptyToken() {
        assertThat(jwtTokenProvider.validateJwtToken("")).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void validateExpiredToken() {
        // Set expiration to -1ms (already expired)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000);
        String token = jwtTokenProvider.generateTokenFromUsername("expired@example.com");

        // Reset expiration for validation
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000);
        assertThat(jwtTokenProvider.validateJwtToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should return false for tampered token")
    void validateTamperedToken() {
        String token = jwtTokenProvider.generateTokenFromUsername("test@example.com");
        // Corrupt the last few characters of the signature
        String tampered = token.substring(0, token.length() - 5) + "ZZZZZ";

        // Tampered tokens should be rejected (either returns false or throws)
        try {
            boolean result = jwtTokenProvider.validateJwtToken(tampered);
            assertThat(result).isFalse();
        } catch (Exception e) {
            // If validateJwtToken doesn't catch SignatureException, 
            // the exception itself confirms the token is rejected
            assertThat(e).isNotNull();
        }
    }
}
