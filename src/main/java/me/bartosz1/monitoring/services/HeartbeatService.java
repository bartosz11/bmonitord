package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Heartbeat;
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

    //todo add user verification here?
    public Heartbeat findLastByMonitorId(long monitorId) throws EntityNotFoundException {
        Page<Heartbeat> heartbeats = heartbeatRepository.findByMonitorIdOrderByTimestampDesc(monitorId, PageRequest.of(0, 1));
        if (!heartbeats.isEmpty()) {
            Optional<Heartbeat> optionalHeartbeat = heartbeats.get().findFirst();
            if (optionalHeartbeat.isPresent()) {
                return optionalHeartbeat.get();
            }
        }
        throw new EntityNotFoundException("No heartbeats found for monitor with ID " + monitorId);
    }

    public Heartbeat findById(long id) throws EntityNotFoundException {
        Optional<Heartbeat> optionalHeartbeat = heartbeatRepository.findById(id);
        if (optionalHeartbeat.isPresent()) {
            return optionalHeartbeat.get();
        }
        throw new EntityNotFoundException("No heartbeat with ID " + id + " found");
    }

    public Page<Heartbeat> findHeartbeatPageByMonitorId(long monitorId, Pageable pageable) throws EntityNotFoundException {
        Page<Heartbeat> heartbeatPage = heartbeatRepository.findByMonitorIdOrderByTimestampDesc(monitorId, pageable);
        if (!heartbeatPage.isEmpty()) {
            return heartbeatPage;
        }
        throw new EntityNotFoundException("No heartbeats found for monitor with ID " + monitorId);
    }
}