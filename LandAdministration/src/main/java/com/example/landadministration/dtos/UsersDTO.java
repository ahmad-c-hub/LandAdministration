package com.example.landadministration.dtos;

import lombok.Getter;

public class UsersDTO {

    @Getter
    private String username;

    @Getter
    private String role;

    @Getter
    private boolean isGoogleUser;

    @Getter
    private String country;

    public UsersDTO(String username, String role, boolean isGoogleUser, String country) {
        this.username = username;
        this.role = role;
        this.isGoogleUser = isGoogleUser;
        this.country = country;
    }

    public String toString(){
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", isGoogleUser=" + isGoogleUser +
                ", country='" + country + '\'' +
                '}';
    }
}
