package com.example.backend.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRentalRequest(
        @NotNull Long professionalId,
        @NotNull Long roomId,
        @NotBlank String yearMonth
) {
}
