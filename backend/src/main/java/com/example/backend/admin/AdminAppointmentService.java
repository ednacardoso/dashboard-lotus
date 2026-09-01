package com.example.backend.admin;

import com.example.backend.appointment.Appointment;
import com.example.backend.appointment.AppointmentRepository;
import com.example.backend.room.RoomRentalRepository;
import org.springframework.stereotype.Service;


import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RoomRentalRepository roomRentalRepository;

    public AdminAppointmentService(AppointmentRepository appointmentRepository,
                                     RoomRentalRepository roomRentalRepository) {
        this.appointmentRepository = appointmentRepository;
        this.roomRentalRepository = roomRentalRepository;
    }

    public List<AdminAppointmentResponse> listAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();

        return appointments.stream()
                .sorted(Comparator
                        .comparing((Appointment a) -> a.getAvailability().getDate())
                        .thenComparing(a -> a.getAvailability().getStartTime())
                        .reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AdminAppointmentResponse toResponse(Appointment appointment) {
        String yearMonth = appointment.getAvailability().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        String roomName = roomRentalRepository.findFirstByProfessionalAndYearMonthAndActiveTrue(
                appointment.getProfessional(), yearMonth)
                .map(rental -> rental.getRoom().getName())
                .orElse(null);

        return new AdminAppointmentResponse(
                appointment.getId(),
                appointment.getClient().getName(),
                appointment.getClient().getEmail(),
                appointment.getProfessional().getName(),
                appointment.getProfessional().getSpecialty(),
                appointment.getAvailability().getDate(),
                appointment.getAvailability().getStartTime(),
                appointment.getAvailability().getEndTime(),
                appointment.getStatus().name(),
                roomName
        );
    }
}
