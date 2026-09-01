package com.example.backend.admin;

import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import com.example.backend.util.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminClientService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CreateUserResponse createClient(CreateClientRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("E-mail já cadastrado");
        }

        boolean generated = request.password() == null || request.password().isBlank();
        String password = generated ? PasswordGenerator.generateStrongPassword() : request.password();

        PasswordGenerator.validatePasswordStrength(password);

        User client = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(password))
                .role(Role.CLIENT)
                .build();

        User saved = userRepository.save(client);

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
}
