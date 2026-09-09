package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.auth.AuthLoginRequestDto;
import com.appfinace.api.dto.auth.AuthLoginResponseDto;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.repositories.UserRepository;
import com.appfinace.api.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User existUser;

    @BeforeEach
    public void setUp() {
        existUser = new User();
        existUser.setEmail("joao@email.com");
        existUser.setPassword("password-hashed");
    }

    @Test
    public void shouldLoginSuccessfully() {
        AuthLoginRequestDto dto = new AuthLoginRequestDto("joao@email.com", "pass1234");

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existUser));
        when(passwordEncoder.matches("pass1234", "password-hashed")).thenReturn(true);
        when(jwtService.generateToken("joao@email.com")).thenReturn("jwt-token-generated");

        AuthLoginResponseDto result = authService.login(dto);

        assertThat(result.token()).isEqualTo("jwt-token-generated");
    }

    @Test
    public void shouldThrowBadRequestWhenEmailNotFound() {
        AuthLoginRequestDto dto = new AuthLoginRequestDto("notexists@email.com", "pass1234");

        when(userRepository.findByEmail("notexists@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email ou senha inválidos");

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    public void shouldThrowBadRequestWhenPasswordIsIncorrect() {
        AuthLoginRequestDto dto = new AuthLoginRequestDto("joao@email.com", "pass0000");

        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existUser));
        when(passwordEncoder.matches("pass0000", "password-hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email ou senha inválidos");

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    public void shouldReturnSameErrorMessageForBothInvalidEmailAndInvalidPassword() {
        AuthLoginRequestDto dtoInvalidEmail = new AuthLoginRequestDto("notexists@email.com", "pass1234");
        when(userRepository.findByEmail("notexists@email.com")).thenReturn(Optional.empty());

        AuthLoginRequestDto dtoInvalidPassword = new AuthLoginRequestDto("joao@email.com", "passIncorrect");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existUser));
        when(passwordEncoder.matches("passIncorrect", "password-hashed")).thenReturn(false);

        String messageInvalidEmail = catchException(() -> authService.login(dtoInvalidEmail));
        String messageInvalidPassword = catchException(() -> authService.login(dtoInvalidPassword));

        assertThat(messageInvalidEmail).isEqualTo(messageInvalidPassword);
    }

    private String catchException(Runnable action) {
        try {
            action.run();
            return null;
        } catch (ResponseStatusException ex) {
            return ex.getReason();
        }
    }
}
