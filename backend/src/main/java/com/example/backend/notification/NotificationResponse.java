package com.example.backend.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long appointmentId,
        String type,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
}
