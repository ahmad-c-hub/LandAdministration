package com.example.landadministration.repos;

import com.example.landadministration.entities.UserLog;
import com.example.landadministration.entities.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogRepo extends JpaRepository<UserLog, Integer> {

    @Query("SELECT u FROM UserLog u ORDER BY u.timestamp DESC")
    Page<UserLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT u FROM UserLog u WHERE u.user.id = ?1 ORDER BY u.timestamp DESC")
    Page<UserLog> findByUser_Id(Integer userId, Pageable pageable);

    @Query("SELECT u FROM UserLog u WHERE u.user.username = ?1 ORDER BY u.timestamp DESC")
    Page<UserLog> findByUsername(String username, Pageable pageable);

}
