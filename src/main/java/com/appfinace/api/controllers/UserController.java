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
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.dto.user.UpdatePasswordRequestDto;
import com.appfinace.api.dto.user.UpdateUserRequestDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.service.UserService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<Void> createUser(@Valid @ModelAttribute UserRequestDto data) {
        userService.createUser(data);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<UserResponseDto>> listUsersFiltred(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {
        List<UserResponseDto> data = userService.listUsers(page, size, email, name);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findUser(@PathVariable UUID id) {
        UserResponseDto data = userService.findUser(id);

        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @Valid @ModelAttribute UpdateUserRequestDto body) {
        userService.updateUser(id, body);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/profile-images-user/{id}")
    public ResponseEntity<List<ProfileImagesResponseDto>> getProfileImagesByUser(@PathVariable UUID id) {
        List<ProfileImagesResponseDto> data = userService.getProfileImagesByUser(id);

        return ResponseEntity.ok(data);
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody UpdatePasswordRequestDto body) {
        UUID id = user.getUser().getId();

        userService.updatePassword(id, body);

        return ResponseEntity.ok().build();
    }
}
