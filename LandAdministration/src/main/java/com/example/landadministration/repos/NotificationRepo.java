package com.example.landadministration.repos;

import com.example.landadministration.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Integer> {


    Optional<Notification> findTopByMessageContainingAndReceiver_UsernameOrderByIssuedAtDesc(String keyword, String receiverUsername);

    List<Notification> findByReceiver_IdOrderByIssuedAtDesc(Integer receiverId);

    @Query("select n from Notification n where n.message like concat('%',?1,'%')")
    List<Notification> findNotificationsByCountry(String country);

}
