package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Statuspage;
import me.bartosz1.monitoring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatuspageRepository extends JpaRepository<Statuspage, Long> {
    @Query("SELECT s FROM Statuspage s LEFT JOIN FETCH s.monitors WHERE s.user = :user")
    Iterable<Statuspage> findAllByUser(User user);

    @Query("SELECT s FROM Statuspage s LEFT JOIN FETCH s.monitors LEFT JOIN FETCH s.announcement WHERE s.id = :id")
    Optional<Statuspage> getById(long id);
}
