package com.example.backend.professional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateClientProfessionalRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String password
) {
}
