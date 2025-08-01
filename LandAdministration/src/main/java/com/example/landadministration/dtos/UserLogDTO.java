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
    private Integer id;
    private String role;
    private String action;
    private LocalDateTime timestamp;
    private String description;
    private String country;

    @Override
    public String toString(){
        return "UserLogDTO{" +
                "username='" + username + '\'' +
                ", id=" + id +
                ", role='" + role + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
