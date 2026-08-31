package com.example.backend.config;

import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = User.builder()
                        .name("Administrador")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
            }

            if (userRepository.findByEmail("cliente@example.com").isEmpty()) {
                User client = User.builder()
                        .name("Cliente Teste")
                        .email("cliente@example.com")
                        .password(passwordEncoder.encode("cliente123"))
                        .role(Role.CLIENT)
                        .build();
                userRepository.save(client);
            }

            if (userRepository.findByEmail("profissional@example.com").isEmpty()) {
                User professional = User.builder()
                        .name("Profissional Teste")
                        .email("profissional@example.com")
                        .password(passwordEncoder.encode("profissional123"))
                        .role(Role.PROFESSIONAL)
                        .build();
                userRepository.save(professional);
            }
        };
    }
}
