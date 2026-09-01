package com.example.backend.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank String name,
        String description,
        Integer capacity,
        @NotNull @PositiveOrZero BigDecimal monthlyPrice
) {
}
