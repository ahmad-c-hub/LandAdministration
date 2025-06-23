package com.example.landadministration.repos;

import com.example.landadministration.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role, Integer> {

    @Query("select r from Role r where r.name = ?1")
    Optional<Role> findByName(String name);
}

