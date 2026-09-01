package com.example.backend.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professional/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal UserDetails userDetails) {
        List<Notification> notifications = notificationService.listByUser(userDetails.getUsername());
        return ResponseEntity.ok(notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> listUnread(@AuthenticationPrincipal UserDetails userDetails) {
        List<Notification> notifications = notificationService.listUnreadByUser(userDetails.getUsername());
        return ResponseEntity.ok(notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.countUnreadByUser(userDetails.getUsername()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getAppointment().getId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
