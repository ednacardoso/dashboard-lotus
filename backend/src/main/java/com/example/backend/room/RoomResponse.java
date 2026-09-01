package com.example.backend.room;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RoomResponse(
        Long id,
        String name,
        String description,
        Integer capacity,
        BigDecimal monthlyPrice,
        boolean active,
        LocalDateTime createdAt
) {
}
