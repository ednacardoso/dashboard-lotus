package com.example.backend.room;

public record RoomOccupancyResponse(
        Long roomId,
        String roomName,
        Long professionalId,
        String professionalName,
        String professionalSpecialty,
        String yearMonth
) {
}
