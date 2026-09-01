package com.example.backend.notification;

import com.example.backend.appointment.Appointment;
import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyNewAppointment(Appointment appointment) {
        String message = String.format(
                "Novo agendamento de %s para %s às %s",
                appointment.getClient().getName(),
                appointment.getAvailability().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                appointment.getAvailability().getStartTime()
        );

        Notification notification = Notification.builder()
                .user(appointment.getProfessional())
                .appointment(appointment)
                .type(NotificationType.NEW_APPOINTMENT)
                .message(message)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyCancellation(Appointment appointment) {
        String message = String.format(
                "Agendamento de %s para %s às %s foi cancelado",
                appointment.getClient().getName(),
                appointment.getAvailability().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                appointment.getAvailability().getStartTime()
        );

        Notification notification = Notification.builder()
                .user(appointment.getProfessional())
                .appointment(appointment)
                .type(NotificationType.CANCELLATION)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    public List<Notification> listByUser(String email) {
        User user = loadUser(email);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> listUnreadByUser(String email) {
        User user = loadUser(email);
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    public long countUnreadByUser(String email) {
        User user = loadUser(email);
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long id, String email) {
        User user = loadUser(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Notificação não pertence a este usuário");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private User loadUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getRole() != Role.PROFESSIONAL) {
            throw new RuntimeException("Acesso restrito a profissionais");
        }

        return user;
    }
}
