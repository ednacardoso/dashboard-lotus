package com.example.backend.admin;

import com.example.backend.user.Role;
import com.example.backend.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminProfessionalService adminProfessionalService;
    private final AdminClientService adminClientService;
    private final AdminAppointmentService adminAppointmentService;
    private final UserRepository userRepository;

    public AdminController(AdminProfessionalService adminProfessionalService,
                          AdminClientService adminClientService,
                          AdminAppointmentService adminAppointmentService,
                          UserRepository userRepository) {
        this.adminProfessionalService = adminProfessionalService;
        this.adminClientService = adminClientService;
        this.adminAppointmentService = adminAppointmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/professionals")
    public ResponseEntity<CreateUserResponse> createProfessional(
            @RequestBody @Valid CreateProfessionalRequest request) {
        CreateUserResponse response = adminProfessionalService.createProfessional(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/professionals")
    public ResponseEntity<List<AdminUserResponse>> listProfessionals() {
        List<AdminUserResponse> response = userRepository.findByRole(Role.PROFESSIONAL).stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getSpecialty(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/professionals/{id}")
    public ResponseEntity<AdminUserResponse> updateProfessional(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProfessionalRequest request) {
        AdminUserResponse response = adminProfessionalService.updateProfessional(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/professionals/{id}")
    public ResponseEntity<Void> deleteProfessional(@PathVariable Long id) {
        adminProfessionalService.deleteProfessional(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clients")
    public ResponseEntity<CreateUserResponse> createClient(
            @RequestBody @Valid CreateClientRequest request) {
        CreateUserResponse response = adminClientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<AdminUserResponse>> listClients() {
        List<AdminUserResponse> response = userRepository.findByRole(Role.CLIENT).stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getSpecialty(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<AdminUserResponse> updateClient(
            @PathVariable Long id,
            @RequestBody @Valid UpdateClientRequest request) {
        AdminUserResponse response = adminClientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        adminClientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AdminAppointmentResponse>> listAppointments() {
        return ResponseEntity.ok(adminAppointmentService.listAllAppointments());
    }
}
