package com.example.landadministration.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserLogDTO {
    private String username;
    private String role;
    private String action;
    private LocalDateTime timestamp;
    private String description;

    @Override
    public String toString(){
        return "UserLogDTO{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
