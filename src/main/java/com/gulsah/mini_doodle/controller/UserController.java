package com.gulsah.mini_doodle.controller;

import com.gulsah.mini_doodle.dto.CreateUserRequest;
import com.gulsah.mini_doodle.dto.UserResponse;
import com.gulsah.mini_doodle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(request.username(), request.email()));
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.getUser(id));
    }
}
