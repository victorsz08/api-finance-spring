package com.appfinace.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.auth.AuthLoginRequestDto;
import com.appfinace.api.dto.auth.AuthLoginResponseDto;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.repositories.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEnconder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEnconder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEnconder = passwordEnconder;
        this.jwtService = jwtService;
    }

    public AuthLoginResponseDto login(AuthLoginRequestDto data) {
        User user = this.userRepository.findByEmail(data.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ou senha inválidos"));

        if (!passwordEnconder.matches(data.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ou senha inválidos");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthLoginResponseDto(token);
    }
}
