package com.appfinace.api.controllers;

import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Value;
import com.appfinace.api.dto.auth.AuthLoginRequestDto;
import com.appfinace.api.dto.auth.AuthLoginResponseDto;
import com.appfinace.api.dto.user.UserResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.AuthService;
import com.appfinace.api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação do usuário")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Value("${app.cookie.secure}")
    private Boolean isProduction;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(summary = "Login", description = "EndPoint de login, o token de acesso é setado no cookie httpsOnly", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Usuário ou senha inválidos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody AuthLoginRequestDto data) {
        AuthLoginResponseDto response = this.authService.login(data);

        ResponseCookie cookie = ResponseCookie.from("access_token", response.token())
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .path("/")
                .maxAge(60 * 60 * 24)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .build();
    }

    @Operation(summary = "Logout", description = "Remove o cookie de acesso do usuário", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .build();
    }

    @Operation(summary = "Dados do usuário autenticado", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserDetailsImpl userDetatails) {
        UUID id = userDetatails.getUser().getId();
        UserResponseDto data = this.userService.findUser(id);

        return ResponseEntity.ok(data);
    }
}
