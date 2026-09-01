package com.example.backend.availability;

import com.example.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByProfessionalAndBookedFalseOrderByDateAscStartTimeAsc(User professional);

    List<Availability> findByProfessionalOrderByDateAscStartTimeAsc(User professional);

    List<Availability> findByProfessionalAndDateOrderByStartTimeAsc(User professional, LocalDate date);

    List<Availability> findByProfessionalIdAndBookedFalseOrderByDateAscStartTimeAsc(Long professionalId);

    Optional<Availability> findByIdAndBookedFalse(Long id);
}
