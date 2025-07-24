package com.example.landadministration.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor

public class OwnershipHistoryId implements Serializable {

    @Column(name = "land_id")
    private Integer landId;

    @Column(name = "owner_id")
    private Integer ownerId;

}
