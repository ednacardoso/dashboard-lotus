package com.example.backend.admin;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        String role,
        String specialty,
        LocalDateTime createdAt
) {
}
