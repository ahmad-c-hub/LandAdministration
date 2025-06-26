package com.example.landadministration.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LandDTO {
    private Integer id;

    private String location;

    private double surfaceArea;

    private String usageType;

    private String locationCoordinates;

    public LandDTO(Integer id, String location, double surfaceArea, String usageType) {
        this.id = id;
        this.location = location;
        this.surfaceArea = surfaceArea;
        this.usageType = usageType;
    }
    public void setLocationCoordinates(double latitude, double longitude) {
        this.locationCoordinates = String.format("%.6f, %.6f", latitude, longitude);
    }

    public String toString() {
        return "LandDTO{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", surfaceArea=" + surfaceArea +
                ", usageType='" + usageType + '\'' +
                ", locationCoordinates='" + locationCoordinates + '\'' +
                '}';
    }

}

