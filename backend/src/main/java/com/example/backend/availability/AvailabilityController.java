package com.example.backend.availability;

import com.example.backend.appointment.AvailabilityResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professional/availabilities")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> list(@AuthenticationPrincipal UserDetails userDetails) {
        List<Availability> availabilities = availabilityService.listAllByProfessional(userDetails.getUsername());
        return ResponseEntity.ok(availabilities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponse> create(@RequestBody @Valid AvailabilityRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Availability availability = availabilityService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(toResponse(availability));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<AvailabilityResponse>> createBatch(@RequestBody @Valid BatchAvailabilityRequest request,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        List<Availability> availabilities = availabilityService.createBatch(userDetails.getUsername(), request);
        return ResponseEntity.ok(availabilities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityResponse> update(@PathVariable Long id,
                                                     @RequestBody @Valid AvailabilityRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Availability availability = availabilityService.update(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(toResponse(availability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        availabilityService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    private AvailabilityResponse toResponse(Availability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getDate(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }
}
