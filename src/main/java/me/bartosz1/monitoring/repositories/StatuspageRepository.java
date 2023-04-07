package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Statuspage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatuspageRepository extends JpaRepository<Statuspage, Long> {
    @Query("SELECT s FROM Statuspage s LEFT JOIN FETCH s.monitors WHERE s.user.id = :userId")
    Iterable<Statuspage> findAllByUserId(@Param("userId") long userId);

    @Query("SELECT s FROM Statuspage s LEFT JOIN FETCH s.monitors LEFT JOIN FETCH s.announcement WHERE s.id = :id")
    Optional<Statuspage> getById(long id);
}
