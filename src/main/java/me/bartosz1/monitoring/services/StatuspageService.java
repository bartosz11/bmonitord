package me.bartosz1.monitoring.services;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.repositories.StatuspageAnnouncementRepository;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import me.bartosz1.monitoring.repositories.WhiteLabelDomainRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class StatuspageService {

    private final StatuspageRepository statuspageRepository;
    private final StatuspageAnnouncementRepository statuspageAnnouncementRepository;
    private final MonitorRepository monitorRepository;
    private final WhiteLabelDomainRepository whiteLabelDomainRepository;

    public StatuspageService(StatuspageRepository statuspageRepository, StatuspageAnnouncementRepository statuspageAnnouncementRepository, MonitorRepository monitorRepository, WhiteLabelDomainRepository whiteLabelDomainRepository) {
        this.statuspageRepository = statuspageRepository;
        this.statuspageAnnouncementRepository = statuspageAnnouncementRepository;
        this.monitorRepository = monitorRepository;
        this.whiteLabelDomainRepository = whiteLabelDomainRepository;
    }

    public Statuspage createStatuspage(User user, StatuspageCDO cdo) throws IllegalParameterException {
        if (cdo.getName() == null) throw new IllegalParameterException("Name cannot be null.");
        Statuspage statuspage = new Statuspage(cdo, user);
        List<Long> monitorIds = cdo.getMonitorIds();
        if (!monitorIds.isEmpty()) {
            List<Monitor> monitors = monitorRepository.findAllById(monitorIds);
            //check access
            Set<Monitor> monitorsAllowed = new HashSet<>();
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
        Optional<Statuspage> byId = statuspageRepository.getById(id);
        if (byId.isPresent()) {
            Statuspage statuspage = byId.get();
            if (statuspage.getUser().getId() == user.getId()) {
                return statuspage;
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + id + " not found.");
    }

    public Iterable<Statuspage> getAllStatuspagesByUser(User user) {
        return statuspageRepository.findAllByUserId(user.getId());
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
                if (announcement != null) {
                    statuspage.setAnnouncement(null);
                    statuspageRepository.save(statuspage);
                    statuspageAnnouncementRepository.delete(announcement);
                    return announcement;
                }
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + statuspageId + " not found, or it doesn't have an announcement.");
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

    public Statuspage bindDomainToStatuspage(long statuspageId, long domainId, User user) throws EntityNotFoundException, IllegalParameterException {
        Optional<WhiteLabelDomain> optionalDomain = whiteLabelDomainRepository.findById(domainId);
        Optional<Statuspage> optionalStatuspage = statuspageRepository.getById(statuspageId);
        if (optionalStatuspage.isPresent() && optionalDomain.isPresent()) {
            WhiteLabelDomain whiteLabelDomain = optionalDomain.get();
            Statuspage statuspage = optionalStatuspage.get();
            long userId = user.getId();
            if (whiteLabelDomain.getUser().getId() == userId && statuspage.getUser().getId() == userId) {
                if (statuspage.getWhiteLabelDomain() != null)
                    throw new IllegalParameterException("Statuspage with given ID has a domain bound to it already.");
                if (whiteLabelDomain.getStatuspage() != null)
                    throw new IllegalParameterException("Domain with given ID has a statuspage bound to it already.");
                statuspage.setWhiteLabelDomain(whiteLabelDomain);
                whiteLabelDomain.setStatuspage(statuspage);
                whiteLabelDomainRepository.save(whiteLabelDomain);
                return statuspageRepository.save(statuspage);
            }
        }
        throw new EntityNotFoundException("Statuspage or domain with given ID not found.");
    }

    public Statuspage unbindDomainToStatuspage(long statuspageId, User user) throws EntityNotFoundException {
        Optional<Statuspage> optionalStatuspage = statuspageRepository.getById(statuspageId);
        if (optionalStatuspage.isPresent()) {
            Statuspage statuspage = optionalStatuspage.get();
            WhiteLabelDomain whiteLabelDomain = statuspage.getWhiteLabelDomain();
            long userId = user.getId();
            if ((whiteLabelDomain != null && whiteLabelDomain.getUser().getId() == userId) && statuspage.getUser().getId() == userId) {
                statuspage.setWhiteLabelDomain(null);
                whiteLabelDomain.setStatuspage(null);
                statuspageRepository.save(statuspage);
                whiteLabelDomainRepository.save(whiteLabelDomain);
                return statuspage;
            }
        }
        throw new EntityNotFoundException("Statuspage with given ID not found, or it doesn't have a white label domain bound.");
    }

    public PublicStatuspage getStatuspageAsPublicObject(String id) throws EntityNotFoundException {
        try {
            long idLong = Long.parseLong(id);
            Optional<Statuspage> byId = statuspageRepository.findById(idLong);
            if (byId.isPresent()) {
                Statuspage statuspage = byId.get();
                return new PublicStatuspage(statuspage);
            }
        } catch (NumberFormatException e) {
            Optional<WhiteLabelDomain> byDomain = whiteLabelDomainRepository.findByDomain(id);
            if (byDomain.isPresent()) {
                WhiteLabelDomain whiteLabelDomain = byDomain.get();
                if (whiteLabelDomain.getStatuspage() != null) {
                    long idLong = whiteLabelDomain.getStatuspage().getId();
                    Optional<Statuspage> byId = statuspageRepository.findById(idLong);
                    if (byId.isPresent()) {
                        Statuspage statuspage = byId.get();
                        return new PublicStatuspage(statuspage);
                    }
                }
            }
        }
        throw new EntityNotFoundException("Statuspage with ID " + id + " not found.");
    }
}
