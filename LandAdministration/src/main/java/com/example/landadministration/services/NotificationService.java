package com.example.landadministration.services;

import com.example.landadministration.dtos.NotificationDTO;
import com.example.landadministration.entities.Notification;
import com.example.landadministration.repos.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepo notificationRepo;

    public List<NotificationDTO> getNotificationsForUser(Integer userId) {
        return notificationRepo.findByReceiver_IdOrderByIssuedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void markNotificationAsRead(Integer notificationId, Integer userId) {
        Notification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification not found"));

        if (!n.getReceiver().getId().equals(userId)) {
            throw new IllegalStateException("Unauthorized to modify this notification");
        }

        if (!n.isRead()) {
            n.setRead(true);
            notificationRepo.save(n);
        }
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setSenderId(n.getSender().getId());
        dto.setReceiverId(n.getReceiver().getId());
        dto.setRead(n.isRead());
        dto.setIssuedAt(n.getIssuedAt());
        return dto;
    }

    public List<NotificationDTO> getAll() {
        return notificationRepo.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
