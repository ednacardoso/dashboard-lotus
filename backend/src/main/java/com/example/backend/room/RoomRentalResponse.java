package com.example.backend.room;

public record RoomRentalResponse(
        Long id,
        Long professionalId,
        String professionalName,
        String professionalSpecialty,
        Long roomId,
        String roomName,
        String yearMonth,
        boolean active
) {
}
