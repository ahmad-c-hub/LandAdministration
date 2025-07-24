package com.example.landadministration.repos;

import com.example.landadministration.entities.Land;
import com.example.landadministration.entities.LandOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandOwnerRepo extends JpaRepository<LandOwner, Integer> {

    @Query("select l from LandOwner l where l.firstName = ?1 and l.lastName = ?2")
    Optional<LandOwner> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("select l from LandOwner l where l.emailAddress = ?1")
    Optional<LandOwner> findByEmailAddress(String emailAddress);
}
