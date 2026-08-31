package com.example.backend.auth;

public record AuthResponse(
        String token,
        String email,
        String name
) {
}
