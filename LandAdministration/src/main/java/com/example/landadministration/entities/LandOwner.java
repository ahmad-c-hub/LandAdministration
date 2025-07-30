package com.example.landadministration.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "land_owner")
@Getter
@Setter
public class LandOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_nb")
    private String phoneNb;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "country")
    private String country;

    @OneToMany(mappedBy = "landOwner")
    private List<Land> lands;

    public LandOwner() {}

    public LandOwner(String firstName, String lastName, String phoneNumber, String emailAddress, LocalDate dateOfBirth, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNb = phoneNumber;
        this.emailAddress = emailAddress;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
    }

    public String toString() {
        return "LandOwner{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNb + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", country='" + country + '\'' +
                '}';
    }
}
