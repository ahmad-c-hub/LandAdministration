package com.example.landadministration.dtos;

import com.example.landadministration.entities.LandOwner;
import lombok.*;

@Data
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class LandDTO {
    private Integer id;

    private String location;

    private double surfaceArea;

    private String usageType;

    private String locationCoordinates;

    private LandOwnerDTO currentOwner;

    public LandDTO(Integer id, String location, double surfaceArea, String usageType, LandOwnerDTO landOwner) {
        this.id = id;
        this.location = location;
        this.surfaceArea = surfaceArea;
        this.usageType = usageType;
        this.currentOwner = landOwner;
    }
    public void setLocationCoordinates(double latitude, double longitude) {
        this.locationCoordinates = String.format("%.6f,%.6f", latitude, longitude);
    }

    public String toString() {
        return "LandDTO{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", surfaceArea=" + surfaceArea +
                ", usageType='" + usageType + '\'' +
                ", locationCoordinates='" + locationCoordinates + '\'' +
                ", currentOwner=" + currentOwner +
                '}';
    }

}

