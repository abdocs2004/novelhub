package com.novelhub.dto;

import com.novelhub.entity.Role;

public record AuthResponse(String token, Long id, String name, String email, Role role) {
}
