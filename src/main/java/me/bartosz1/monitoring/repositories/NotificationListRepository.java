package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.NotificationList;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationListRepository extends JpaRepository<NotificationList, Long> {

    Iterable<NotificationList> findAllByUser(User user);
}
