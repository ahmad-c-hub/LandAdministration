package com.example.landadministration.dtos;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime issuedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
