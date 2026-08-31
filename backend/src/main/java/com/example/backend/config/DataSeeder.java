package com.example.backend.config;

import com.example.backend.availability.Availability;
import com.example.backend.availability.AvailabilityRepository;
import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository,
                                AvailabilityRepository availabilityRepository,
                                PasswordEncoder passwordEncoder) {
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

            User professional = userRepository.findByEmail("profissional@example.com").orElse(null);
            if (professional == null) {
                professional = User.builder()
                        .name("Profissional Teste")
                        .email("profissional@example.com")
                        .password(passwordEncoder.encode("profissional123"))
                        .role(Role.PROFESSIONAL)
                        .specialty("Psicologia")
                        .build();
                professional = userRepository.save(professional);
            }

            if (professional.getSpecialty() == null) {
                professional.setSpecialty("Psicologia");
                professional = userRepository.save(professional);
            }

            if (availabilityRepository.count() == 0) {
                LocalDate today = LocalDate.now();
                LocalTime[] slots = {
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(14, 0),
                        LocalTime.of(15, 0)
                };

                for (int dayOffset = 1; dayOffset <= 7; dayOffset++) {
                    LocalDate date = today.plusDays(dayOffset);
                    for (LocalTime start : slots) {
                        Availability availability = Availability.builder()
                                .professional(professional)
                                .date(date)
                                .startTime(start)
                                .endTime(start.plusHours(1))
                                .booked(false)
                                .build();
                        availabilityRepository.save(availability);
                    }
                }
            }
        };
    }
}
