package com.appfinace.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.user.FindUserResponseDto;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<Void> createUser(@ModelAttribute UserRequestDto data) {
        this.userService.createUser(data);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<FindUserResponseDto>> listUsersFiltred(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {
        List<FindUserResponseDto> data = this.userService.listUsers(page, size, email, name);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindUserResponseDto> findUser(@PathVariable UUID id) {
        FindUserResponseDto data = this.userService.findUser(id);

        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @ModelAttribute UserRequestDto body) {
        this.userService.updateUser(id, body.name(), body.email(), body.profileImage());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/profile-images-user/{id}")
    public ResponseEntity<List<ProfileImagesResponseDto>> getProfileImagesByUser(@PathVariable UUID id) {
        List<ProfileImagesResponseDto> data = this.userService.getProfileImagesByUser(id);

        return ResponseEntity.ok(data);
    } 
}
