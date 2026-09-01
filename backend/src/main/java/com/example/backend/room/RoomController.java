package com.example.backend.room;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> listAll() {
        return ResponseEntity.ok(roomService.listAll());
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@RequestBody @Valid RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> update(@PathVariable Long id, @RequestBody @Valid RoomRequest request) {
        return ResponseEntity.ok(roomService.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RoomResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vacant")
    public ResponseEntity<List<RoomResponse>> listVacant(@RequestParam String yearMonth) {
        return ResponseEntity.ok(roomService.listVacantRooms(yearMonth));
    }

    @GetMapping("/occupied")
    public ResponseEntity<List<RoomOccupancyResponse>> listOccupied(@RequestParam String yearMonth) {
        return ResponseEntity.ok(roomService.listOccupiedRooms(yearMonth));
    }

    @GetMapping("/rentals")
    public ResponseEntity<List<RoomRentalResponse>> listRentals(@RequestParam String yearMonth) {
        return ResponseEntity.ok(roomService.listRentals(yearMonth));
    }

    @PostMapping("/rentals")
    public ResponseEntity<RoomRentalResponse> rentRoom(@RequestBody @Valid RoomRentalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.rentRoom(request));
    }

    @PatchMapping("/rentals/{id}/remove")
    public ResponseEntity<Void> removeRental(@PathVariable Long id) {
        roomService.removeRental(id);
        return ResponseEntity.noContent().build();
    }
}
