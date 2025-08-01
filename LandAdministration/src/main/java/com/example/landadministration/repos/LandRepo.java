package com.example.landadministration.repos;

import com.example.landadministration.dtos.LandDTO;
import com.example.landadministration.entities.Land;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LandRepo extends JpaRepository<Land, Integer>, JpaSpecificationExecutor<Land> {


    @Query("select l from Land l where l.id = ?1 and l.location like concat('%', ?2)")
    Optional<Land> findByIdAndCountry(Integer id, String country);

    @Query("select l from Land l where l.location like concat('%', ?1)")
    Page<Land> findByCountry(String country, Pageable pageable);

    @Query("select l from Land l where l.surfaceArea >= ?1 and l.surfaceArea <= ?2")
    List<Land> filterBySurfaceArea(double min, double max, Sort sort);

    @Query("select l from Land l where l.usage_type = ?1")
    List<Land> findByUsageType(String usageType, Sort sort);

    @Query("select l from Land l where l.latitude = ?1 and l.longitude = ?2")
    Optional<Land> findByLocationCoordinates(double latitude, double longitude);

    @Query("select l from Land l where l.location = ?1")
    List<Land> findByLocation(String location,Sort sort);

    @Query("select l from Land l where l.location = ?1")
    Page<Land> findByLocationPage(String location, Pageable pageable);

    @Query("select l from Land l where l.surfaceArea >= ?1 and l.surfaceArea <= ?2")
    Page<Land> filterBySurfaceAreaPage(double min, double max, Pageable pageable);

    @Query("select l from Land l where l.surfaceArea >= ?1 and l.surfaceArea <= ?2 and l.location like concat('%',?3) ")
    Page<Land> filterBySurfaceAreaAndCountry(double min, double max, String country, Pageable pageable);



    @Query("select l from Land l")
    Page<Land> findAllLandsPaged(Pageable pageable);

    @Query("select l from Land l where l.usage_type = ?1")
    Page<Land> findByUsageTypePaged(String usageType, Pageable pageable);

    @Query("select l from Land l where l.landOwner.id = ?1")
    Page<Land> findLandsByOwnerId(Integer ownerId, Pageable pageable);

    @Query("select l from Land l where l.landOwner is null")
    List<Land> getUnassignedLands();
}
