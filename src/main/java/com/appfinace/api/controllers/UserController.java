package com.appfinace.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.service.UserService;


@Controller
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
    
}
