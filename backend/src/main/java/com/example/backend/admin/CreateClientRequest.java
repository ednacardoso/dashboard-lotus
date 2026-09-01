package com.example.backend.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres") String password
) {
}
