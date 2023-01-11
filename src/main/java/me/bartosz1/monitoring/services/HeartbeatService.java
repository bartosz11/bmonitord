package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.HeartbeatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HeartbeatService {

    private final HeartbeatRepository heartbeatRepository;

    public HeartbeatService(HeartbeatRepository heartbeatRepository) {
        this.heartbeatRepository = heartbeatRepository;
    }

    public Heartbeat saveHeartbeat(Heartbeat heartbeat) {
        return heartbeatRepository.save(heartbeat);
    }

    public Heartbeat findLastByMonitorId(long monitorId, User user) throws EntityNotFoundException {
        Page<Heartbeat> heartbeats = heartbeatRepository.findByMonitorIdOrderByTimestampDesc(monitorId, PageRequest.of(0, 1));
        if (!heartbeats.isEmpty()) {
            Optional<Heartbeat> optionalHeartbeat = heartbeats.get().findFirst();
            if (optionalHeartbeat.isPresent()) {
                Heartbeat heartbeat = optionalHeartbeat.get();
                Monitor monitor = heartbeat.getMonitor();
                if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) {
                    return heartbeat;
                }
            }
        }
        throw new EntityNotFoundException("No heartbeats found for monitor with ID " + monitorId);
    }

    public Heartbeat findById(long id, User user) throws EntityNotFoundException {
        Optional<Heartbeat> optionalHeartbeat = heartbeatRepository.findById(id);
        if (optionalHeartbeat.isPresent()) {
            Heartbeat heartbeat = optionalHeartbeat.get();
            Monitor monitor = heartbeat.getMonitor();
            if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) {
                return heartbeat;
            }
        }
        throw new EntityNotFoundException("No heartbeat with ID " + id + " found");
    }

    public Page<Heartbeat> findHeartbeatPageByMonitorId(long monitorId, Pageable pageable, User user) throws EntityNotFoundException {
        Page<Heartbeat> heartbeatPage = heartbeatRepository.findByMonitorIdOrderByTimestampDesc(monitorId, pageable);
        if (!heartbeatPage.isEmpty()) {
            Optional<Heartbeat> first = heartbeatPage.stream().findFirst();
            if (first.isPresent()) {
                Heartbeat heartbeat = first.get();
                Monitor monitor = heartbeat.getMonitor();
                if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) {
                    return heartbeatPage;
                }
            }
        }
        throw new EntityNotFoundException("No heartbeats found for monitor with ID " + monitorId);
    }

    public Page<Heartbeat> findHeartbeatPageByMonitorIdAndTimestampRange(long monitorId, Pageable pageable, User user, long timestampMin, long timestampMax) throws EntityNotFoundException {
        Page<Heartbeat> heartbeatPage = heartbeatRepository.findByMonitorIdAndTimestampBetweenOrderByTimestampDesc(monitorId, pageable, timestampMin, timestampMax);
        if (!heartbeatPage.isEmpty()) {
            //we check ownership on first one only if needed
            Optional<Heartbeat> first = heartbeatPage.stream().findFirst();
            if (first.isPresent()) {
                Heartbeat heartbeat = first.get();
                Monitor monitor = heartbeat.getMonitor();
                if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) {
                    return heartbeatPage;
                }
            }
        }
        throw new EntityNotFoundException("No heartbeats found for monitor with ID" + monitorId + " between given timestamps.");
    }
}