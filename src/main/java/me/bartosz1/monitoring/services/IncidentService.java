package me.bartosz1.monitoring.services;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.repositories.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public Incident findByIdAndUser(long id, User user) throws EntityNotFoundException {
        Optional<Incident> optionalIncident = incidentRepository.findById(id);
        if (optionalIncident.isPresent()) {
            Incident incident = optionalIncident.get();
            Monitor monitor = incident.getMonitor();
            if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) return incident;
        }
        throw new EntityNotFoundException("Incident with ID " + id + " not found.");
    }

    public Incident findLastByMonitorIdAndUser(long monitorId, User user) throws EntityNotFoundException {
        Page<Incident> incidentPage = incidentRepository.findByMonitorIdOrderByStartTimestampDesc(monitorId, PageRequest.of(0, 1));
        if (!incidentPage.isEmpty()) {
            Optional<Incident> optionalIncident = incidentPage.get().findFirst();
            if (optionalIncident.isPresent()) {
                Incident incident = optionalIncident.get();
                Monitor monitor = incident.getMonitor();
                if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) return incident;
            }
        }
        throw new EntityNotFoundException("No incidents found for monitor with ID " + monitorId + ".");
    }

    public Page<Incident> findIncidentPageByMonitorIdAndUser(long monitorId, User user, Pageable pageable) throws EntityNotFoundException {
        Page<Incident> incidentPage = incidentRepository.findByMonitorIdOrderByStartTimestampDesc(monitorId, pageable);
        if (!incidentPage.isEmpty()) {
            //Check for ownership only on first incident of the page
            Optional<Incident> optionalIncident = incidentPage.get().findFirst();
            if (optionalIncident.isPresent()) {
                Incident incident = optionalIncident.get();
                Monitor monitor = incident.getMonitor();
                if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) return incidentPage;
            }
        }
        throw new EntityNotFoundException("No incidents found for monitor with ID " + monitorId + ".");
    }
}
