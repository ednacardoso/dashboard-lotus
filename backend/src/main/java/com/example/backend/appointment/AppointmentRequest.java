package com.example.backend.appointment;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
        @NotNull Long availabilityId
) {
}
