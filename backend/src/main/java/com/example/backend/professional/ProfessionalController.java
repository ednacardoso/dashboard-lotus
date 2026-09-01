package com.example.backend.professional;

import com.example.backend.admin.AdminClientService;
import com.example.backend.admin.CreateClientRequest;
import com.example.backend.admin.CreateUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professional")
@PreAuthorize("hasRole('PROFESSIONAL')")
public class ProfessionalController {

    private final AdminClientService adminClientService;

    public ProfessionalController(AdminClientService adminClientService) {
        this.adminClientService = adminClientService;
    }

    @PostMapping("/clients")
    public ResponseEntity<CreateUserResponse> createClient(
            @RequestBody @Valid CreateClientProfessionalRequest request) {
        CreateUserResponse response = adminClientService.createClient(
                new CreateClientRequest(request.name(), request.email(), request.password())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
