package com.example.landadministration.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OwnershipHistoryDTO {

    private LandDTO land;
    private LandOwnerDTO landOwner;
    private LocalDateTime ownershipStart;
    private LocalDateTime ownershipEnd;
    private LocalDateTime createdAt;
}
