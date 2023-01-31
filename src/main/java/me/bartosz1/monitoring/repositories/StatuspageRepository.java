package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Statuspage;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StatuspageRepository extends JpaRepository<Statuspage, Long> {
    @Query("SELECT s FROM Statuspage s LEFT JOIN FETCH s.monitors WHERE s.user = :user")
    Iterable<Statuspage> findAllByUser(User user);
}
