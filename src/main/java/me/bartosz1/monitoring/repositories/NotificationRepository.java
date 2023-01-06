package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Iterable<Notification> findAllByUser(User user);
}
