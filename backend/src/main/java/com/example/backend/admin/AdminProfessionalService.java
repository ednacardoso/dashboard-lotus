package com.example.backend.admin;

import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminProfessionalService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminProfessionalService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfessionalCreatedResponse createProfessional(CreateProfessionalRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("E-mail já cadastrado");
        }

        User professional = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PROFESSIONAL)
                .specialty(request.specialty())
                .build();

        User saved = userRepository.save(professional);

        return new ProfessionalCreatedResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getSpecialty()
        );
    }
}
