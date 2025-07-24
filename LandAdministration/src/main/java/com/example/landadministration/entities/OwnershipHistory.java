package com.example.landadministration.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ownership_history")
@Getter
@Setter
public class OwnershipHistory {

    @EmbeddedId
    private OwnershipHistoryId id;

    @ManyToOne
    @MapsId("landId")
    @JoinColumn(name = "land_id")
    private Land land;

    @ManyToOne
    @MapsId("ownerId")
    @JoinColumn(name = "owner_id")
    private LandOwner owner;

    @Column(name = "ownership_start")
    private LocalDateTime ownershipStart;

    @Column(name = "ownership_end")
    private LocalDateTime ownershipEnd;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public OwnershipHistory() {
    }

    public OwnershipHistory(Land land, LandOwner landOwner, LocalDateTime ownershipStartDate, LocalDateTime createdAt) {
        this.land = land;
        this.owner = landOwner;
        this.ownershipStart = ownershipStartDate;
        this.createdAt = createdAt;
    }
}
