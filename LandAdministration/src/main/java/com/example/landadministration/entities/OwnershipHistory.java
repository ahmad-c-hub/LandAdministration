package com.example.landadministration.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    public OwnershipHistory() {}

    public OwnershipHistory(Integer recordId, Land land, LandOwner owner, LocalDateTime ownershipStart, LocalDateTime createdAt) {
        this.id = new OwnershipHistoryId(recordId, land.getId(), owner.getId());
        this.land = land;
        this.owner = owner;
        this.ownershipStart = ownershipStart;
        this.createdAt = createdAt;
    }
}

