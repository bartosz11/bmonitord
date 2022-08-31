package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.User;
import me.bartosz1.monitoring.models.monitor.Monitor;
import me.bartosz1.monitoring.models.statuspage.Statuspage;
import me.bartosz1.monitoring.models.statuspage.StatuspageAnnouncement;
import me.bartosz1.monitoring.models.statuspage.StatuspageAnnouncementCDO;
import me.bartosz1.monitoring.models.statuspage.StatuspageMonitorObject;
import me.bartosz1.monitoring.repos.MonitorRepository;
import me.bartosz1.monitoring.repos.StatuspageAnnouncementRepository;
import me.bartosz1.monitoring.repos.StatuspageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StatuspageService {

    @Autowired
    private InfluxService influxService;
    @Autowired
    private StatuspageRepository statuspageRepository;
    @Autowired
    private StatuspageAnnouncementRepository statuspageAnnouncementRepository;
    @Autowired
    private MonitorRepository monitorRepository;
    @Autowired
    private IncidentService incidentService;
    @Value("${monitoring.influxdb.enabled}")
    private boolean influxEnabled;

    public Statuspage createStatuspage(User user, String name, List<Long> monitorIds) {
        Statuspage statuspage = new Statuspage().setUser(user).setName(name);
        if (!monitorIds.isEmpty()) {
            List<Monitor> monitors = monitorRepository.findByIds(monitorIds);
            //check access
            List<Monitor> monitorsAllowed = new ArrayList<>();
            monitors.forEach(monitor -> {
                if (monitor.getUser().getId() == user.getId()) {
                    monitor.getStatuspages().add(statuspage);
                    monitorsAllowed.add(monitor);
                }
            });
            statuspage.setMonitors(monitorsAllowed);
        }
        return statuspageRepository.save(statuspage);
    }

    public Statuspage deleteStatuspage(long id, User user) {
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(id);
        if (optionalStatuspage.isPresent()) {
            Statuspage statuspage = optionalStatuspage.get();
            if (statuspage.getUser().getId() == user.getId()) {
                List<Monitor> bulkSaveMonitor = new ArrayList<>();
                if (!statuspage.getMonitors().isEmpty()) {
                    statuspage.getMonitors().forEach(monitor -> {
                        monitor.getStatuspages().remove(statuspage);
                        bulkSaveMonitor.add(monitor);
                    });
                    statuspage.setMonitors(new ArrayList<>());
                }
                monitorRepository.saveAll(bulkSaveMonitor);
                statuspageAnnouncementRepository.deleteAllByStatuspage(statuspage);
                statuspageRepository.delete(statuspage);
                return statuspage;
            }
        }
        return null;
    }

    public Statuspage findStatuspageById(long id) {
        Optional<Statuspage> result = statuspageRepository.findById(id);
        if (result.isEmpty()) return null;
        return result.get();
    }

    public Iterable<Statuspage> findAllByUser(User user) {
        return statuspageRepository.findAllByUser(user);
    }

    public Statuspage findByIdAndUser(long id, User user) {
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(id);
        if (optionalStatuspage.isPresent()) {
            Statuspage statuspage = optionalStatuspage.get();
            if (statuspage.getUser().getId() == user.getId()) return statuspage;
        }
        return null;
    }

    public Statuspage addAnnouncement(User user, StatuspageAnnouncementCDO cdo) {
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(cdo.getStatuspageId());
        if (optionalStatuspage.isPresent()) {
            Statuspage statuspage = optionalStatuspage.get();
            if (statuspage.getUser().getId() == user.getId()) {
                StatuspageAnnouncement statuspageAnnouncement = new StatuspageAnnouncement(cdo, statuspage);
                //This makes modifying possible
                if (statuspage.getAnnouncement() != null)
                    statuspageAnnouncement.setId(statuspage.getAnnouncement().getId());
                statuspage.setAnnouncement(statuspageAnnouncement);
                statuspageAnnouncementRepository.save(statuspageAnnouncement);
                return statuspageRepository.save(statuspage);
            }
        }
        return null;
    }

    public Statuspage removeAnnouncement(User user, long statuspageId) {
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(statuspageId);
        if (optionalStatuspage.isPresent()) {
            Statuspage statuspage = optionalStatuspage.get();
            if (statuspage.getUser().getId() == user.getId()) {
                StatuspageAnnouncement statuspageAnnouncement = statuspage.getAnnouncement();
                statuspage.setAnnouncement(null);
                statuspageAnnouncementRepository.delete(statuspageAnnouncement);
                return statuspageRepository.save(statuspage);
            }
        }
        return null;
    }

    public Statuspage getPublicStats(long id) {
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(id);
        if (optionalStatuspage.isPresent()) {
            Statuspage statuspage = optionalStatuspage.get();
            statuspage.getMonitors().forEach(monitor -> {
                StatuspageMonitorObject statuspageMonitorObject = monitor.toStatuspageObject();
                statuspageMonitorObject.setLastIncidents(incidentService.getFiveIncidentsByMonitorId(null, monitor.getId(), 0).getContent());
                if (influxEnabled) {
                    if (monitor.getAgent() != null)
                        influxService.getLastAgentData(monitor.getAgent().getId(), monitor.getAgent().getLastDataReceived(), statuspageMonitorObject);
                    else
                        influxService.getLastMonitorResponseTime(monitor.getId(), monitor.getLastSuccessfulCheck(), statuspageMonitorObject);
                }
                statuspage.getStatuspageMonitorObjects().add(statuspageMonitorObject);
            });
            return statuspage;
        }
        return null;
    }

}
