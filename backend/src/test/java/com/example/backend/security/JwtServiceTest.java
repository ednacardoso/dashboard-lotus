package com.example.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    private static final String SECRET = "my-32-char-long-secret-for-jwt-service-tests";
    private static final long EXPIRATION_MS = 1000L * 60 * 60; // 1h

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    @Test
    void shouldGenerateTokenWithClaims() {
        String token = jwtService.generateToken("user@example.com", "User Name", "CLIENT");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("CLIENT");
    }

    @Test
    void shouldValidateTokenForMatchingEmail() {
        String token = jwtService.generateToken("user@example.com", "User Name", "CLIENT");

        assertThat(jwtService.validateToken(token, "user@example.com")).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentEmail() {
        String token = jwtService.generateToken("user@example.com", "User Name", "CLIENT");

        assertThat(jwtService.validateToken(token, "other@example.com")).isFalse();
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken("user@example.com", "User Name", "CLIENT");

        Thread.sleep(10);

        assertThat(jwtService.isTokenExpired(token)).isTrue();
        assertThat(jwtService.validateToken(token, "user@example.com")).isFalse();
    }

    @Test
    void shouldExtractExpirationDate() {
        String token = jwtService.generateToken("user@example.com", "User Name", "CLIENT");

        Date expiration = jwtService.extractExpiration(token);
        assertThat(expiration).isAfter(new Date());
    }
}
