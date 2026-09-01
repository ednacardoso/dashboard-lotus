package com.example.backend.appointment;

import com.example.backend.availability.Availability;
import com.example.backend.availability.AvailabilityRepository;
import com.example.backend.notification.NotificationService;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import com.example.backend.user.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AvailabilityRepository availabilityRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<ProfessionalResponse> listProfessionals() {
        return userRepository.findByRole(Role.PROFESSIONAL).stream()
                .map(user -> new ProfessionalResponse(user.getId(), user.getName(), user.getSpecialty()))
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponse> listAvailableSlots(Long professionalId) {
        return availabilityRepository.findByProfessionalIdAndBookedFalseOrderByDateAscStartTimeAsc(professionalId).stream()
                .map(av -> new AvailabilityResponse(av.getId(), av.getDate(), av.getStartTime(), av.getEndTime()))
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse createAppointment(String clientEmail, AppointmentRequest request) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Availability availability = availabilityRepository.findByIdAndBookedFalse(request.availabilityId())
                .orElseThrow(() -> new RuntimeException("Horário indisponível ou não encontrado"));

        availability.setBooked(true);
        availabilityRepository.save(availability);

        Appointment appointment = Appointment.builder()
                .client(client)
                .professional(availability.getProfessional())
                .availability(availability)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointment = appointmentRepository.save(appointment);
        notificationService.notifyNewAppointment(appointment);
        return toResponse(appointment);
    }

    public List<AppointmentResponse> listMyAppointments(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return appointmentRepository.findByClientOrderByCreatedAtDesc(client).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> listByProfessional(String professionalEmail) {
        User professional = userRepository.findByEmail(professionalEmail)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        if (professional.getRole() != Role.PROFESSIONAL) {
            throw new RuntimeException("Acesso restrito a profissionais");
        }

        return appointmentRepository.findByProfessionalOrderByAvailability_DateDescAvailability_StartTimeAsc(professional).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, String clientEmail, AppointmentRequest request) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Appointment appointment = appointmentRepository.findByIdAndClient(id, client)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Não é possível editar um agendamento cancelado");
        }

        Availability oldAvailability = appointment.getAvailability();
        Availability newAvailability = availabilityRepository.findByIdAndBookedFalse(request.availabilityId())
                .orElseThrow(() -> new RuntimeException("Horário indisponível ou não encontrado"));

        oldAvailability.setBooked(false);
        availabilityRepository.save(oldAvailability);

        newAvailability.setBooked(true);
        availabilityRepository.save(newAvailability);

        appointment.setProfessional(newAvailability.getProfessional());
        appointment.setAvailability(newAvailability);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(Long id, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Appointment appointment = appointmentRepository.findByIdAndClient(id, client)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Agendamento já está cancelado");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getAvailability().setBooked(false);
        availabilityRepository.save(appointment.getAvailability());
        appointmentRepository.save(appointment);
        notificationService.notifyCancellation(appointment);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        Availability availability = appointment.getAvailability();
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getName(),
                appointment.getProfessional().getSpecialty(),
                availability.getDate(),
                availability.getStartTime(),
                availability.getEndTime(),
                appointment.getStatus().name()
        );
    }
}
