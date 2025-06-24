package com.example.landadministration.dtos;

import lombok.Getter;

public class UsersDTO {

    @Getter
    private String username;

    @Getter
    private String role;

    @Getter
    private boolean isGoogleUser;

    public UsersDTO(String username, String role, boolean isGoogleUser) {
        this.username = username;
        this.role = role;
        this.isGoogleUser = isGoogleUser;
    }

    public String toString(){
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", isGoogleUser=" + isGoogleUser +
                '}';
    }
}
