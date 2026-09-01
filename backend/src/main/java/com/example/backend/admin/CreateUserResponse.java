package com.example.backend.admin;

public record CreateUserResponse(
        Long id,
        String name,
        String email,
        String role,
        String specialty,
        boolean passwordGenerated,
        String generatedPassword
) {
}
