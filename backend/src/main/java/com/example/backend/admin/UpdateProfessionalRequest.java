package com.example.backend.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfessionalRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String specialty
) {
}
