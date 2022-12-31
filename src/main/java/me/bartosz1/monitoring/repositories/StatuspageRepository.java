package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Statuspage;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatuspageRepository extends JpaRepository<Statuspage, Long> {

    Iterable<Statuspage> findAllByUser(User user);
}
