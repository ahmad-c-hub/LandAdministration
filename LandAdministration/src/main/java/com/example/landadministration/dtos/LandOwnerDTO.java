package com.example.landadministration.dtos;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LandOwnerDTO {
    private Integer id;
    private String fullName;
    private String phoneNumber;
    private String emailAddress;
    private Integer numberOfLands;
    private Integer age;
    private String country;

    public LandOwnerDTO(Integer id, String fullName, String phoneNumber, String emailAddress, Integer numberOfLands, Integer age, String country) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.numberOfLands = numberOfLands;
        this.age = age;
        this.country = country;
    }

    public String toString() {
        return "LandOwnerDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", numberOfLands=" + numberOfLands +
                ", age=" + age +
                ", country='" + country + '\'' +
                '}';
    }


}
