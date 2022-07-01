package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Page<Incident> findByMonitorIdOrderByStartTimestampDesc(long monitorId, Pageable pageable);

    void deleteAllByMonitorId(long monitorId);
}
