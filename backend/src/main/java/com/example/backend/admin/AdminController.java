package com.example.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminProfessionalService adminProfessionalService;

    public AdminController(AdminProfessionalService adminProfessionalService) {
        this.adminProfessionalService = adminProfessionalService;
    }

    @PostMapping("/professionals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalCreatedResponse> createProfessional(
            @RequestBody @Valid CreateProfessionalRequest request) {
        ProfessionalCreatedResponse response = adminProfessionalService.createProfessional(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
