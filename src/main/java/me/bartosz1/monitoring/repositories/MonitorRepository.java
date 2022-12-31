package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    Iterable<Monitor> findAllByUser(User user);
}
