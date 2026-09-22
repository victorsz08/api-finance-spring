package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.appfinace.api.infra.security.JwtService;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret",
                "chave-secreta-de-teste-com-tamanho-suficiente-para-hmac-sha256");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L); // 1h
    }

    @Test
    public void shouldGenerateTokenAndExtractEmailCorrectly() {
        String token = jwtService.generateToken("joao@email.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("joao@email.com");
    }

    @Test
    public void shouldConsiderFreshlyGeneratedTokenAsValid() {
        String token = jwtService.generateToken("joao@email.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    public void shouldConsiderExpiredTokenAsInvalid() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -10000L);

        String token = jwtService.generateToken("joao@email.com");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    public void shouldConsiderMalformedTokenAsInvalid() {
        assertThat(jwtService.isTokenValid("Its-invalid-token")).isFalse();
    }

    @Test
    public void shouldConsiderTokenSignedWithDifferentSecretAsInvalid() {
        String token = jwtService.generateToken("joao@email.com");

        JwtService otherJwtService = new JwtService();
        ReflectionTestUtils.setField(otherJwtService, "secret", "outra-chave-diferente-da-outra-para-test-hmac-sha256");
        ReflectionTestUtils.setField(otherJwtService, "expirationMs", 3600000L);

        assertThat(otherJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    public void shouldThrowWhenExtractingEmailFromInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractEmail("invalid-token"))
                .isInstanceOf(Exception.class);
    }
}
