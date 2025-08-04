package com.example.landadministration.controllers;
import com.example.landadministration.dtos.NotificationDTO;
import com.example.landadministration.entities.Users;
import com.example.landadministration.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(
        origins = {
                "http://localhost:3000",           // for local development
                "https://ahmad-c-hub.github.io"    // for production (GitHub Pages)
        },
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/my")
    public List<NotificationDTO> getMyNotifications() {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationService.getNotificationsForUser(currentUser.getId());
    }

    @PutMapping("/{id}/mark-read")
    public String markAsRead(@PathVariable Integer id) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.markNotificationAsRead(id, currentUser.getId());
        return "Notification marked as read.";
    }
}
