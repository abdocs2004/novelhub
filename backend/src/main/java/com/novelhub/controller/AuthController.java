package com.novelhub.controller;

import com.novelhub.dto.AuthResponse;
import com.novelhub.dto.LoginRequest;
import com.novelhub.dto.RegisterRequest;
import com.novelhub.dto.UserResponse;
import com.novelhub.security.UserPrincipal;
import com.novelhub.service.AuthService;
import com.novelhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controllers are thin: they receive the HTTP request, validate the input (@Valid), call ONE service method,
 * and return the result. No business logic and no database code here.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Who am I? Requires a valid token. The simplest example of a protected endpoint. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getById(principal.getId());
    }
}
