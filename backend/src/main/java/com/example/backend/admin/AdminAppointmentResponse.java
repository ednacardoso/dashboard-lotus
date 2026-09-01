package com.example.backend.admin;

import java.time.LocalDate;
import java.time.LocalTime;

public record AdminAppointmentResponse(
        Long id,
        String clientName,
        String clientEmail,
        String professionalName,
        String professionalSpecialty,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        String roomName
) {
}
