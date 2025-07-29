package com.example.landadministration.repos;

import com.example.landadministration.dtos.OwnershipHistoryDTO;
import com.example.landadministration.entities.OwnershipHistory;
import com.example.landadministration.entities.OwnershipHistoryId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnershipHistoryRepo extends JpaRepository<OwnershipHistory, OwnershipHistoryId> {

    @Query("SELECT h FROM OwnershipHistory h WHERE h.id.landId = ?1 AND h.ownershipEnd is NULL")
    Optional<OwnershipHistory> findActiveByLandId(Integer landId);


    @Query("SELECT h FROM OwnershipHistory h WHERE h.land.id = :landId")
    List<OwnershipHistory> findByLand_Id(Integer landId);

    @Query("SELECT h FROM OwnershipHistory h WHERE h.owner.id = :ownerId")
    Page<OwnershipHistory> findByOwner_Id(Integer ownerId, Pageable pageable);

    @Query(value = "SELECT nextval('ownership_record_id_seq')", nativeQuery = true)
    Integer getNextRecordId();
    @Query("SELECT h FROM OwnershipHistory h WHERE h.land.id = :landId AND h.owner.id = :ownerId AND h.ownershipEnd is NULL")
    Optional<OwnershipHistory> findActiveByOwner_IdAndLand_Id(Integer ownerId, Integer landId);

}
