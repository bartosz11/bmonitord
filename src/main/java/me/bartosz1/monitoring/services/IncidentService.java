package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repos.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

//All methods in this class are checking access too
@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    public Incident getIncidentById(User user, long id) {
        Optional<Incident> result = incidentRepository.findById(id);
        if (result.isEmpty()) return null;
        //Check for access
        if (result.get().getMonitor().getUser().getId() != user.getId()) return null;
        return result.get();
    }

    public Incident getLastIncidentByMonitorId(User user, long id) {
        Optional<Incident> result = incidentRepository.getByMonitorIdOrderByStartTimestampDesc(id);
        if (result.isEmpty()) return null;
        if (result.get().getMonitor().getUser().getId() != user.getId()) return null;
        return result.get();
    }

    public Page<Incident> getFiveIncidentsByMonitorId(User user, long id, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Incident> result = incidentRepository.findByMonitorIdOrderByStartTimestampDesc(id, pageable);
        if (result.isEmpty()) return null;
        //If user has access to one, then he has access to other ones in the page too
        if (result.get().findFirst().get().getMonitor().getUser().getId() != user.getId()) return null;
        return result;
    }
}
