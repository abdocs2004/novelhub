package com.novelhub.dto;

import com.novelhub.entity.Role;
import com.novelhub.entity.User;

import java.time.Instant;

/** Public view of a user. Notice: no password field, so it can never leak. */
public record UserResponse(Long id, String name, String email, Role role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
