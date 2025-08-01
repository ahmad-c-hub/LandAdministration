package com.example.landadministration.repos;

import com.example.landadministration.entities.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer> {


    @Query("select u from Users u where u.username = ?1")
    Optional<Users>  findByUsername(String username);

    @Query("select u from Users u where u.id = ?1 and u.country = ?2")
    Optional<Users> findByIdAndCountry(Integer id, String country);

    @Query("select u from Users u where u.country = ?1")
    Page<Users> findAllByCountry(String country, Pageable pageable);
}
