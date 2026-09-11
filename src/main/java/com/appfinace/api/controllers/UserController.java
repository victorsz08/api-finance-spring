package com.appfinace.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.user.UserResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.dto.user.UpdatePasswordRequestDto;
import com.appfinace.api.dto.user.UpdateUserRequestDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints de gerenciamento dos usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Criar um novo usuário", responses = {
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))

    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data")))
    @PostMapping()
    public ResponseEntity<Void> createUser(@Valid @ModelAttribute UserRequestDto data) {
        userService.createUser(data);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Lista os usuários filtrados", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<UserResponseDto>> listUsersFiltred(
            @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de elementos por página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filtro parcial por nome") @RequestParam(required = false) String name,
            @Parameter(description = "Filtro parcial por email") @RequestParam(required = false) String email) {
        List<UserResponseDto> data = userService.listUsers(page, size, email, name);

        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Busca usuário por ID", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findUser(@PathVariable UUID id) {
        UserResponseDto data = userService.findUser(id);

        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Atualiza usuário", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "multipart/form-data")))
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @Valid @ModelAttribute UpdateUserRequestDto body) {
        userService.updateUser(id, body);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Lista as imagens de perfil do usuário", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @GetMapping("/profile-images-user/{id}")
    public ResponseEntity<List<ProfileImagesResponseDto>> getProfileImagesByUser(@PathVariable UUID id) {
        List<ProfileImagesResponseDto> data = userService.getProfileImagesByUser(id);

        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Atualiza a senha do usuário", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody UpdatePasswordRequestDto body) {
        UUID id = user.getUser().getId();

        userService.updatePassword(id, body);

        return ResponseEntity.ok().build();
    }
}
