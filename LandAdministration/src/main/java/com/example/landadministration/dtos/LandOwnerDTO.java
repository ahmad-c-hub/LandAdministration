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

    public LandOwnerDTO(Integer id, String fullName, String phoneNumber, String emailAddress, Integer numberOfLands, Integer age) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.numberOfLands = numberOfLands;
        this.age = age;
    }

    public String toString() {
        return "LandOwnerDTO{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", numberOfLands=" + numberOfLands +
                ", age=" + age +
                '}';
    }


}
