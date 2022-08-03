package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repos.IncidentRepository;
import me.bartosz1.monitoring.repos.MonitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//All methods in this class are checking access too
@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private MonitorRepository monitorRepository;

    public Incident getIncidentById(User user, long id) {
        Optional<Incident> result = incidentRepository.findById(id);
        if (result.isEmpty()) return null;
        Incident incident = result.get();
        //Return if public
        if (incident.getMonitor().isPublic()) return incident;
        //Check for access
        if (incident.getMonitor().getUser().getId() != user.getId()) return null;
        return result.get();
    }

    public Incident getLastIncidentByMonitorId(User user, long id) {
        Page<Incident> result = incidentRepository.findByMonitorIdOrderByStartTimestampDesc(id, PageRequest.of(0, 1));
        List<Incident> content = result.getContent();
        if (content.isEmpty()) return null;
        Incident incident = content.get(0);
        if (incident.getMonitor().isPublic()) return incident;
        if (incident.getMonitor().getUser().getId() != user.getId()) return null;
        return incident;
    }

    public Page<Incident> getFiveIncidentsByMonitorId(User user, long id, int page) {
        Page<Incident> result = incidentRepository.findByMonitorIdOrderByStartTimestampDesc(id, PageRequest.of(page, 5));
        //If it's empty then it can be safely returned without any checks
        if (result.isEmpty()) return result;
        Incident first = result.get().findFirst().get();
        //All incidents are bound to the same monitor, so that's why I have to check only the first one
        //any other could be checked too
        if (first.getMonitor().isPublic()) return result;
        //If user has access to one, then he has access to other ones in the page too
        if (first.getMonitor().getUser().getId() != user.getId()) return null;
        return result;
    }
}
