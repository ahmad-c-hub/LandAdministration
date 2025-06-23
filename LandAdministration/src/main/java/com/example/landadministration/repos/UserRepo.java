package com.example.landadministration.repos;

import com.example.landadministration.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer> {


    @Query("select u from Users u where u.username = ?1")
    Optional<Users>  findByUsername(String username);
}
