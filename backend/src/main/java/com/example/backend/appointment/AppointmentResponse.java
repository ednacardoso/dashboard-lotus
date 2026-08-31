package com.example.backend.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        Long professionalId,
        String professionalName,
        String professionalSpecialty,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String status
) {
}
