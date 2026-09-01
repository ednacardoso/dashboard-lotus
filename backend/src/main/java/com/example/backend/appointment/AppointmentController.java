package com.example.backend.appointment;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<ProfessionalResponse>> listProfessionals() {
        return ResponseEntity.ok(appointmentService.listProfessionals());
    }

    @GetMapping("/availabilities")
    public ResponseEntity<List<AvailabilityResponse>> listAvailabilities(@RequestParam Long professionalId) {
        return ResponseEntity.ok(appointmentService.listAvailableSlots(professionalId));
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> create(@RequestBody @Valid AppointmentRequest request,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.createAppointment(userDetails.getUsername(), request));
    }

    @GetMapping("/appointments/my")
    public ResponseEntity<List<AppointmentResponse>> listMyAppointments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.listMyAppointments(userDetails.getUsername()));
    }

    @GetMapping("/professional/appointments")
    public ResponseEntity<List<AppointmentResponse>> listProfessionalAppointments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.listByProfessional(userDetails.getUsername()));
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> update(@PathVariable Long id,
                                                    @RequestBody @Valid AppointmentRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, userDetails.getUsername(), request));
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        appointmentService.cancelAppointment(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
