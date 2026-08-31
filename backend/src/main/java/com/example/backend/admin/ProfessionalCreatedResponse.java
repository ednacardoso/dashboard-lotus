package com.example.backend.admin;

public record ProfessionalCreatedResponse(
        Long id,
        String name,
        String email,
        String specialty
) {
}
