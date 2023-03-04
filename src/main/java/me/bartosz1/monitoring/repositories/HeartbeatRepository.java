package me.bartosz1.monitoring.repositories;

import me.bartosz1.monitoring.models.Heartbeat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartbeatRepository extends JpaRepository<Heartbeat, Long> {

    Page<Heartbeat> findByMonitorId(long monitorId, Pageable pageable);

    Page<Heartbeat> findByMonitorIdAndTimestampBetween(long monitorId, Pageable pageable, long fromTimestamp, long toTimestamp);
    void deleteAllByMonitorId(long monitorId);

}