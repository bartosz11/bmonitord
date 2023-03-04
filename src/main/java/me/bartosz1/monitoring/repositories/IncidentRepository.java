package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Page<Incident> findByMonitorIdOrderByStartTimestampDesc(long monitorId, Pageable pageable);
    void deleteAllByMonitorId(long monitorId);
}
