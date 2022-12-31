package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.models.enums.MonitorType;
import me.bartosz1.monitoring.repositories.AgentRepository;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.repositories.NotificationListRepository;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final AgentRepository agentRepository;
    private final NotificationListRepository notificationListRepository;
    private final StatuspageRepository statuspageRepository;

    public MonitorService(MonitorRepository monitorRepository, AgentRepository agentRepository, NotificationListRepository notificationListRepository,
                          StatuspageRepository statuspageRepository) {
        this.monitorRepository = monitorRepository;
        this.agentRepository = agentRepository;
        this.notificationListRepository = notificationListRepository;
        this.statuspageRepository = statuspageRepository;
    }

    public Monitor createMonitor(MonitorCDO cdo, User user) {
        if (cdo.getType() == MonitorType.AGENT) {
            return monitorRepository.save(new Monitor(cdo, user, Instant.now(), new Agent()));
        }
        return monitorRepository.save(new Monitor(cdo, user, Instant.now()));
    }

    public Monitor deleteMonitor(long id, User user) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) {
                //todo add cascading stuff here if needed
               /* NotificationList notificationList = monitor.getNotificationList();
                notificationList.getMonitors().remove(monitor);
                notificationListRepository.save(notificationList);
                */
                monitorRepository.delete(monitor);
                return monitor;
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Monitor getMonitorByIdAndUser(long id, User user) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) return monitor;
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Iterable<Monitor> getAllMonitorsByUser(User user) {
        return monitorRepository.findAllByUser(user);
    }

    public Monitor renameMonitor(long id, String name, User user) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) {
                monitor.setName(name);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Monitor pauseMonitor(long id, boolean pause, User user) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) {
                monitor.setPaused(pause);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Monitor publishMonitor(long id, boolean publish, User user) throws EntityNotFoundException, IllegalStateException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) {
                if (!monitor.getStatuspages().isEmpty() && !publish)
                    //subject-to-change maybe use some wrapper of IllegalStateException????
                    throw new IllegalStateException("Monitors can't be unpublished when assigned to any statuspage.");
                monitor.setPublished(publish);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Monitor assignNotificationListToMonitor(User user, long monitorId, long notificationListId) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(monitorId);
        Optional<NotificationList> optionalNotificationList = notificationListRepository.findById(notificationListId);
        if (optionalMonitor.isPresent() && optionalNotificationList.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            NotificationList notificationList = optionalNotificationList.get();
            if (monitor.getUser().getId() == user.getId() && notificationList.getUser().getId() == user.getId()) {
                monitor.setNotificationList(notificationList);
                notificationList.getMonitors().add(monitor);
                notificationListRepository.save(notificationList);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor or notification list with given ID not found.");
    }

    public Monitor unassignNotificationListFromMonitor(User user, long monitorId) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(monitorId);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            NotificationList notificationList = monitor.getNotificationList();
            if (monitor.getUser().getId() == user.getId()) {
                //technically it has to be assigned somehow, but better safe than sorry or something
                if (notificationList != null) {
                    notificationList.getMonitors().remove(monitor);
                    notificationListRepository.save(notificationList);
                    monitor.setNotificationList(null);
                    monitor = monitorRepository.save(monitor);
                }
                return monitor;
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + monitorId + " not found.");
    }

    public Monitor assignMonitorToStatuspage(User user, long monitorId, long pageId) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(monitorId);
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(pageId);
        if (optionalMonitor.isPresent() && optionalStatuspage.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            Statuspage statuspage = optionalStatuspage.get();
            if (monitor.getUser().getId() == user.getId() && statuspage.getUser().getId() == user.getId()) {
                monitor.setPublished(true);
                //prevent duplicates
                if (!statuspage.getMonitors().contains(monitor)) statuspage.getMonitors().add(monitor);
                if (!monitor.getStatuspages().contains(statuspage)) monitor.getStatuspages().add(statuspage);
                statuspageRepository.save(statuspage);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor or statuspage with given ID not found.");
    }

    public Monitor unassignMonitorFromStatuspage(User user, long monitorId, long pageId) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(monitorId);
        Optional<Statuspage> optionalStatuspage = statuspageRepository.findById(pageId);
        if (optionalMonitor.isPresent() && optionalStatuspage.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            Statuspage statuspage = optionalStatuspage.get();
            if (statuspage.getUser().getId() == user.getId() && monitor.getUser().getId() == user.getId()) {
                //i don't think we need any duplicate protection here but might be subject-to-change
                monitor.getStatuspages().remove(statuspage);
                statuspage.getMonitors().remove(monitor);
                statuspageRepository.save(statuspage);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor or statuspage with given ID not found.");
    }
}
