package me.bartosz1.monitoring.services;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.repositories.StatuspageAnnouncementRepository;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StatuspageService {

    private final StatuspageRepository statuspageRepository;
    private final StatuspageAnnouncementRepository statuspageAnnouncementRepository;
    private final MonitorRepository monitorRepository;

    public StatuspageService(StatuspageRepository statuspageRepository, StatuspageAnnouncementRepository statuspageAnnouncementRepository, MonitorRepository monitorRepository) {
        this.statuspageRepository = statuspageRepository;
        this.statuspageAnnouncementRepository = statuspageAnnouncementRepository;
        this.monitorRepository = monitorRepository;
    }

    public Statuspage createStatuspage(User user, StatuspageCDO cdo) throws IllegalParameterException {
        if (cdo.getName() == null) throw new IllegalParameterException("Name cannot be null.");
        Statuspage statuspage = new Statuspage(cdo, user);
        List<Long> monitorIds = cdo.getMonitorIds();
        if (!monitorIds.isEmpty()) {
            List<Monitor> monitors = monitorRepository.findAllById(monitorIds);
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

    public Statuspage deleteStatuspage(long id, User user) throws EntityNotFoundException {
        Optional<Statuspage> byId = statuspageRepository.findById(id);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            if (statuspage.getUser().getId() == user.getId()) {
                if (statuspage.getAnnouncement() != null)
                    statuspageAnnouncementRepository.delete(statuspage.getAnnouncement());
                List<Monitor> bulkSaveMonitor = new ArrayList<>();
                statuspage.getMonitors().forEach(monitor -> {
                    monitor.getStatuspages().remove(statuspage);
                    bulkSaveMonitor.add(monitor);
                });
                monitorRepository.saveAll(bulkSaveMonitor);
                statuspageRepository.delete(statuspage);
                return statuspage;
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + id + " not found.");
    }

    public Statuspage getStatuspageByIdAndUser(long id, User user) throws EntityNotFoundException {
        Optional<Statuspage> byId = statuspageRepository.findById(id);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            if (statuspage.getUser().getId() == user.getId()) {
                return statuspage;
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + id + " not found.");
    }

    public Iterable<Statuspage> getAllStatuspagesByUser(User user) {
        return statuspageRepository.findAllByUser(user);
    }

    public StatuspageAnnouncement addAnnouncement(User user, StatuspageAnnouncementCDO cdo, long pageId) throws EntityNotFoundException {
        Optional<Statuspage> byId = statuspageRepository.findById(pageId);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            if (statuspage.getUser().getId() == user.getId()) {
                StatuspageAnnouncement announcement = new StatuspageAnnouncement(cdo, statuspage);
                //This makes modifying possible
                if (statuspage.getAnnouncement() != null)
                    announcement.setId(statuspage.getAnnouncement().getId());
                statuspage.setAnnouncement(announcement);
                statuspageRepository.save(statuspage);
                return statuspageAnnouncementRepository.save(announcement);
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + pageId + " not found.");
    }

    public StatuspageAnnouncement removeAnnouncement(User user, long statuspageId) throws EntityNotFoundException {
        Optional<Statuspage> byId = statuspageRepository.findById(statuspageId);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            if (statuspage.getUser().getId() == user.getId()) {
                StatuspageAnnouncement announcement = statuspage.getAnnouncement();
                statuspage.setAnnouncement(null);
                statuspageRepository.save(statuspage);
                statuspageAnnouncementRepository.delete(announcement);
                return announcement;
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + statuspageId + " not found.");
    }

    public Statuspage modifyStatuspage(User user, long id, StatuspageCDO cdo) throws EntityNotFoundException, IllegalParameterException {
        if (cdo.getName() == null) throw new IllegalParameterException("Name cannot be null.");
        Optional<Statuspage> byId = statuspageRepository.getById(id);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            if (statuspage.getUser().getId() == user.getId()) {
                statuspage.setLogoLink(cdo.getLogoLink());
                statuspage.setLogoRedirect(cdo.getLogoRedirect());
                statuspage.setName(cdo.getName());
                return statuspageRepository.save(statuspage);
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + id + " not found.");
    }

    public PublicStatuspage getStatuspageAsPublicObject(long id) throws EntityNotFoundException {
        Optional<Statuspage> byId = statuspageRepository.findById(id);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            return new PublicStatuspage(statuspage);
        }
        throw new EntityNotFoundException("Statuspage with ID " + id + " not found.");
    }
}
