package com.novelhub.service;

import com.novelhub.dto.AuthResponse;
import com.novelhub.dto.LoginRequest;
import com.novelhub.dto.RegisterRequest;
import com.novelhub.entity.Role;
import com.novelhub.entity.User;
import com.novelhub.exception.ConflictException;
import com.novelhub.repository.UserRepository;
import com.novelhub.security.JwtService;
import com.novelhub.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("This email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password())); // hash, never store the raw password
        user.setRole(Role.USER); // public registration can NEVER create an admin
        userRepository.save(user);

        return toAuthResponse(UserPrincipal.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        // Checks the password (BCrypt) using CustomUserDetailsService. Throws BadCredentialsException if wrong,
        // which GlobalExceptionHandler turns into a 401.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));

        return toAuthResponse((UserPrincipal) authentication.getPrincipal());
    }

    private AuthResponse toAuthResponse(UserPrincipal principal) {
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, principal.getId(), principal.getName(), principal.getUsername(), principal.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
