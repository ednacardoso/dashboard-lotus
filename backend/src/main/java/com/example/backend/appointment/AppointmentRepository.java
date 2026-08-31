package com.example.backend.appointment;

import com.example.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByClientOrderByCreatedAtDesc(User client);

    List<Appointment> findByClientAndStatusOrderByCreatedAtDesc(User client, AppointmentStatus status);

    Optional<Appointment> findByIdAndClient(Long id, User client);
}
