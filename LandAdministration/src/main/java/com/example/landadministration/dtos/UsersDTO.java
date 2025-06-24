package com.example.landadministration.dtos;

import lombok.Getter;

public class UsersDTO {

    @Getter
    private String username;
    @Getter
    private String role;

    public UsersDTO(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String toString(){
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
