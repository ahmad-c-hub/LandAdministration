package com.example.landadministration.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "land")
@Getter
@Setter
public class Land {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String location;

    private double longitude;

    private double latitude;

    @Column(name = "surface_area")
    private double surfaceArea;

    private String usage_type;


    @ManyToOne
    @JoinColumn(name="land_owner_id")
    private LandOwner landOwner;

    public Land() {}

    public Land(String location, double longitude, double latitude, double surface_area, String usage_type) {
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
        this.surfaceArea = surface_area;
        this.usage_type = usage_type;
    }

    public String getCountryFromLocation(String location) {
        if (location == null || !location.contains(",")) {
            return "Unknown";
        }
        String[] parts = location.split(",");
        return parts[parts.length - 1].trim();
    }


    public String toString() {
        return "Land{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", surface_area=" + surfaceArea +
                ", usage_type='" + usage_type + '\'' +
                ", landOwner=" + landOwner +
                '}';
    }
}
