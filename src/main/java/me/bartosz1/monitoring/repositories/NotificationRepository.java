package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.monitors WHERE n.user = :user")
    Iterable<Notification> findAllByUser(User user);
}
