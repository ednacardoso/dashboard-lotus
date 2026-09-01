package com.example.backend.admin;

import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import com.example.backend.util.PasswordGenerator;
import jakarta.persistence.EntityNotFoundException;
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

    public CreateUserResponse createProfessional(CreateProfessionalRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("E-mail já cadastrado");
        }

        boolean generated = request.password() == null || request.password().isBlank();
        String password = generated ? PasswordGenerator.generateStrongPassword() : request.password();

        PasswordGenerator.validatePasswordStrength(password);

        User professional = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(password))
                .role(Role.PROFESSIONAL)
                .specialty(request.specialty())
                .build();

        User saved = userRepository.save(professional);

        return new CreateUserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getSpecialty(),
                generated,
                password
        );
    }

    public AdminUserResponse updateProfessional(Long id, UpdateProfessionalRequest request) {
        User professional = userRepository.findById(id)
                .filter(user -> user.getRole() == Role.PROFESSIONAL)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        if (!professional.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("E-mail já cadastrado");
        }

        professional.setName(request.name());
        professional.setEmail(request.email());
        professional.setSpecialty(request.specialty());

        User updated = userRepository.save(professional);

        return new AdminUserResponse(
                updated.getId(),
                updated.getName(),
                updated.getEmail(),
                updated.getRole().name(),
                updated.getSpecialty(),
                updated.getCreatedAt()
        );
    }

    public void deleteProfessional(Long id) {
        User professional = userRepository.findById(id)
                .filter(user -> user.getRole() == Role.PROFESSIONAL)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        userRepository.delete(professional);
    }
}
