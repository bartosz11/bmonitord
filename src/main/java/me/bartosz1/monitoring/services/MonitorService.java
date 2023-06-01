package me.bartosz1.monitoring.services;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.exceptions.EntityNotFoundException;
import me.bartosz1.monitoring.exceptions.IllegalParameterException;
import me.bartosz1.monitoring.models.*;
import me.bartosz1.monitoring.models.enums.MonitorType;
import me.bartosz1.monitoring.models.monitor.MonitorHTTPInfo;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.repositories.NotificationRepository;
import me.bartosz1.monitoring.repositories.StatuspageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final NotificationRepository notificationRepository;
    private final StatuspageRepository statuspageRepository;

    public MonitorService(MonitorRepository monitorRepository, NotificationRepository notificationRepository, StatuspageRepository statuspageRepository) {
        this.monitorRepository = monitorRepository;
        this.notificationRepository = notificationRepository;
        this.statuspageRepository = statuspageRepository;
    }

    public Monitor createMonitor(MonitorCDO cdo, User user) {
        switch (cdo.getType()) {
            case AGENT -> {
                return monitorRepository.save(new Monitor(cdo, user, Instant.now(), new Agent()));
            }
            //subject-to-change - might simplify this later
            case HTTP -> {
                Monitor monitor = new Monitor(cdo, user, Instant.now());
                MonitorHTTPInfo httpInfo = new MonitorHTTPInfo(cdo.getHttpInfoCDO(), monitor);
                monitor.setHttpInfo(httpInfo);
                return monitorRepository.save(monitor);
            }
            //ping
            default -> {
                return monitorRepository.save(new Monitor(cdo, user, Instant.now()));
            }
        }
    }

    public Monitor deleteMonitor(long id, User user) throws EntityNotFoundException {
        Optional<Monitor> optionalMonitor = monitorRepository.findById(id);
        if (optionalMonitor.isPresent()) {
            Monitor monitor = optionalMonitor.get();
            if (monitor.getUser().getId() == user.getId()) {
                List<Statuspage> bulkSaveStatuspage = new ArrayList<>();
                monitor.getStatuspages().forEach(statuspage -> {
                    statuspage.getMonitors().remove(monitor);
                    bulkSaveStatuspage.add(statuspage);
                });
                statuspageRepository.saveAll(bulkSaveStatuspage);
                List<Notification> bulkSaveNotification = new ArrayList<>();
                monitor.getNotifications().forEach(notification -> {
                    notification.getMonitors().remove(monitor);
                    bulkSaveNotification.add(notification);
                });
                notificationRepository.saveAll(bulkSaveNotification);
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
            if ((user != null && monitor.getUser().getId() == user.getId()) || monitor.isPublished()) {
                return monitor;
            }
        }
        throw new EntityNotFoundException("Monitor with ID " + id + " not found.");
    }

    public Iterable<Monitor> getAllMonitorsByUser(User user) {
        return monitorRepository.findAllByUserId(user.getId());
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
                    throw new IllegalParameterException("Monitors can't be made private when assigned to a statuspage.");
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

    public Agent getAgentByMonitorId(long monitorId, User user) throws EntityNotFoundException {
        Optional<Monitor> byId = monitorRepository.findById(monitorId);
        if (byId.isPresent()) {
            Monitor monitor = byId.get();
            if (monitor.getType() == MonitorType.AGENT) {
                Agent agent = monitor.getAgent();
                if (user != null && monitor.getUser().getId() == user.getId()) {
                    return agent;
                } else if (monitor.isPublished()) {
                    return new Agent().setAgentVersion(agent.getAgentVersion()).setCpuCores(agent.getCpuCores()).setCpuModel(agent.getCpuModel()).setInstalled(agent.isInstalled()).setIpAddress(agent.getIpAddress()).setLastDataReceived(agent.getLastDataReceived()).setOs(agent.getOs()).setRamTotal(agent.getRamTotal()).setSwapTotal(agent.getSwapTotal()).setUptime(agent.getUptime());
                }
            }
        }
        throw new EntityNotFoundException("Agent not found for monitor with ID " + monitorId + ".");
    }
}
