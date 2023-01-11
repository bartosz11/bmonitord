package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.models.enums.MonitorType;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.repositories.NotificationRepository;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final NotificationRepository notificationRepository;
    private final StatuspageRepository statuspageRepository;

    public MonitorService(MonitorRepository monitorRepository, NotificationRepository notificationRepository,
                          StatuspageRepository statuspageRepository) {
        this.monitorRepository = monitorRepository;
        this.notificationRepository = notificationRepository;
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

    public Monitor publishMonitor(long id, boolean publish, User user) throws EntityNotFoundException, IllegalStateException, IllegalParameterException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) {
                if (!monitor.getStatuspages().isEmpty() && !publish)
                    throw new IllegalParameterException("Monitors can't be unpublished when assigned to any statuspage.");
                monitor.setPublished(publish);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Monitor assignNotficationToMonitor(User user, long monitorId, long notificationId) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(monitorId);
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalMonitor.isPresent() && optionalNotification.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            Notification notification = optionalNotification.get();
            if (monitor.getUser().getId() == user.getId() && notification.getUser().getId() == user.getId()) {
                if (!monitor.getNotifications().contains(notification)) monitor.getNotifications().add(notification);
                if (!notification.getMonitors().contains(monitor)) notification.getMonitors().add(monitor);
                notificationRepository.save(notification);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor or notification with given ID not found.");
    }

    public Monitor unassignNotificationFromMonitor(User user, long monitorId, long notificationId) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(monitorId);
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalMonitor.isPresent() && optionalNotification.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            Notification notification = optionalNotification.get();
            if (monitor.getUser().getId() == user.getId() && notification.getUser().getId() == user.getId()) {
                monitor.getNotifications().remove(notification);
                notification.getMonitors().remove(monitor);
                notificationRepository.save(notification);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor or notification with given ID not found.");
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
                monitor.getStatuspages().remove(statuspage);
                statuspage.getMonitors().remove(monitor);
                statuspageRepository.save(statuspage);
                return monitorRepository.save(monitor);
            }
        }
        throw new EntityNotFoundException("Monitor or statuspage with given ID not found.");
    }
}
