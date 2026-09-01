package com.example.backend.availability;

import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository, UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    public List<Availability> listByProfessional(String email) {
        User professional = loadProfessional(email);
        return availabilityRepository.findByProfessionalAndBookedFalseOrderByDateAscStartTimeAsc(professional);
    }

    public List<Availability> listAllByProfessional(String email) {
        User professional = loadProfessional(email);
        return availabilityRepository.findByProfessionalOrderByDateAscStartTimeAsc(professional);
    }

    @Transactional
    public Availability create(String email, AvailabilityRequest request) {
        User professional = loadProfessional(email);
        validateTimes(request.date(), request.startTime(), request.endTime());
        validateNoOverlap(professional, request.date(), request.startTime(), request.endTime(), null);

        Availability availability = Availability.builder()
                .professional(professional)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .booked(false)
                .build();

        return availabilityRepository.save(availability);
    }

    @Transactional
    public List<Availability> createBatch(String email, BatchAvailabilityRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("A data final deve ser posterior ou igual à data inicial");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("O horário de término deve ser posterior ao horário de início");
        }

        if (request.daysOfWeek().stream().anyMatch(day -> day < 1 || day > 7)) {
            throw new IllegalArgumentException("Dias da semana devem estar entre 1 (domingo) e 7 (sábado)");
        }

        User professional = loadProfessional(email);
        List<Availability> created = new java.util.ArrayList<>();

        LocalDate currentDate = request.startDate();
        while (!currentDate.isAfter(request.endDate())) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue() % 7 + 1;

            if (request.daysOfWeek().contains(dayOfWeek)) {
                LocalTime slotStart = request.startTime();
                while (slotStart.isBefore(request.endTime())) {
                    LocalTime slotEnd = slotStart.plusMinutes(request.slotDurationMinutes());
                    if (slotEnd.isAfter(request.endTime())) {
                        break;
                    }

                    validateNoOverlap(professional, currentDate, slotStart, slotEnd, null);

                    Availability availability = Availability.builder()
                            .professional(professional)
                            .date(currentDate)
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .booked(false)
                            .build();

                    created.add(availabilityRepository.save(availability));
                    slotStart = slotEnd;
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        return created;
    }

    @Transactional
    public Availability update(Long id, String email, AvailabilityRequest request) {
        User professional = loadProfessional(email);
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        if (!availability.getProfessional().getId().equals(professional.getId())) {
            throw new RuntimeException("Horário não pertence a este profissional");
        }

        if (availability.isBooked()) {
            throw new RuntimeException("Não é possível editar um horário já reservado");
        }

        validateTimes(request.date(), request.startTime(), request.endTime());
        validateNoOverlap(professional, request.date(), request.startTime(), request.endTime(), id);

        availability.setDate(request.date());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());

        return availabilityRepository.save(availability);
    }

    @Transactional
    public void delete(Long id, String email) {
        User professional = loadProfessional(email);
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horário não encontrado"));

        if (!availability.getProfessional().getId().equals(professional.getId())) {
            throw new RuntimeException("Horário não pertence a este profissional");
        }

        if (availability.isBooked()) {
            throw new RuntimeException("Não é possível excluir um horário já reservado");
        }

        availabilityRepository.delete(availability);
    }

    private User loadProfessional(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getRole() != Role.PROFESSIONAL) {
            throw new RuntimeException("Acesso restrito a profissionais");
        }

        return user;
    }

    private void validateTimes(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("O horário de término deve ser posterior ao horário de início");
        }
    }

    private void validateNoOverlap(User professional, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeId) {
        List<Availability> availabilities = availabilityRepository.findByProfessionalAndDateOrderByStartTimeAsc(professional, date);

        for (Availability existing : availabilities) {
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }

            boolean overlaps = startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime());
            if (overlaps) {
                throw new RuntimeException("O horário informado conflita com um horário já cadastrado");
            }
        }
    }
}
