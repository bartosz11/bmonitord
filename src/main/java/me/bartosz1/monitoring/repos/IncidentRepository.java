package me.bartosz1.monitoring.repos;

import me.bartosz1.monitoring.models.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

   Optional<Incident> getByMonitorIdOrderByStartTimestampDesc(long monitorId);
   Page<Incident> findByMonitorIdOrderByStartTimestampDesc(long monitorId, Pageable pageable);
}
