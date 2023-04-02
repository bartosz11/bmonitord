package me.bartosz1.monitoring;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.repositories.IncidentRepository;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.services.NotificationSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MonitorChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorChecker.class);
    private final MonitorRepository monitorRepository;
    private final NotificationSenderService notificationSenderService;
    private final IncidentRepository incidentRepository;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<Long, Integer> retries = new ConcurrentHashMap<>();

    public MonitorChecker(MonitorRepository monitorRepository, NotificationSenderService notificationSenderService, IncidentRepository incidentRepository, @Value("${monitoring.check-thread-pool-size:2}") int checkThreadPoolSize) {
        this.monitorRepository = monitorRepository;
        this.notificationSenderService = notificationSenderService;
        this.incidentRepository = incidentRepository;
        this.executorService = Executors.newFixedThreadPool(checkThreadPoolSize, new CustomizableThreadFactory("check-pool-"));
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkMonitors() throws InterruptedException {
        LOGGER.info("Checking monitors...");
        //very brh
        List<Monitor> allMonitors = monitorRepository.findAllMonitors2(monitorRepository.findAllMonitors(monitorRepository.findAllMonitors()));
        List<Monitor> bulkSaveMonitors = new ArrayList<>();
        //monitors which are not paused
        List<Monitor> active = allMonitors.stream().filter(monitor -> !monitor.isPaused()).toList();
        CountDownLatch latch = new CountDownLatch(active.size());
        active.forEach(monitor -> executorService.execute(() -> {
            LOGGER.debug("Checking " + monitor.getName() + " ID " + monitor.getId() + " type " + monitor.getType().name());
            //idk i found this somewhere
            TransactionSynchronizationManager.setActualTransactionActive(true);
            Heartbeat hb = monitor.getType().getCheckProvider().check(monitor);
            MonitorStatus currentStatus = hb.getStatus();
            //.getCheckProvider().check has to return a heartbeat but the heartbeat shouldn't always be saved
            if (hb.getMonitor() != null) {
                LOGGER.debug("Adding new heartbeat for monitor ID " + monitor.getId());
                monitor.getHeartbeats().add(hb);
            }
            //hb.getStatus(currentStatus) can be null if agent is not installed/5 min since the app start didn't pass
            if (currentStatus != null) {
                LOGGER.debug("Check provider for ID " + monitor.getId() + " returned " + currentStatus);
                if (monitor.getType().applyRetriesLogic()) {
                    if (currentStatus == MonitorStatus.DOWN) {
                        retries.merge(monitor.getId(), 1, Integer::sum);
                        //Works properly even when monitor.getRetries() == 0 because 1 > 0
                        if (retries.get(monitor.getId()) > monitor.getRetries()) {
                            processStatus(monitor, currentStatus);
                        }
                    } else {
                        retries.remove(monitor.getId());
                        processStatus(monitor, currentStatus);
                    }
                } else {
                    processStatus(monitor, currentStatus);
                }
                bulkSaveMonitors.add(monitor);
            }
            latch.countDown();
        }));
        latch.await();
        LOGGER.debug("All checks finished, saving");
        monitorRepository.saveAll(bulkSaveMonitors);
        LOGGER.info("Saved monitors.");
    }

    //mostly copied from "monitoring" (v1 branch)
    private void processStatus(Monitor monitor, MonitorStatus currentStatus) {
        long epochSecond = Instant.now().getEpochSecond();
        incrementChecksAndChangeTimestamps(monitor, currentStatus, epochSecond);
        monitor.setLastStatus(currentStatus);
        //notification sending logic
        //Get incident sorted by start timestamp descending
        List<Incident> incidents = monitor.getIncidents().stream().sorted(Comparator.comparing(Incident::getStartTimestamp).reversed()).toList();
        if (currentStatus == MonitorStatus.UP) {
            if (incidents.isEmpty()) {
                return;
            }
            Incident lastIncident = incidents.get(0);
            if (lastIncident.isOngoing()) {
                lastIncident.setOngoing(false);
                lastIncident.setEndTimestamp(epochSecond);
                lastIncident.setDuration(lastIncident.getEndTimestamp() - lastIncident.getStartTimestamp());
                notificationSenderService.sendNotifications(monitor, incidentRepository.save(lastIncident));
            }
        }
        if (currentStatus == MonitorStatus.DOWN) {
            if (!incidents.isEmpty()) {
                Incident lastIncident = incidents.get(0);
                if (lastIncident.isOngoing()) {
                    return;
                }
            }
            Incident incident = new Incident().setStartTimestamp(epochSecond).setOngoing(true).setMonitor(monitor);
            monitor.getIncidents().add(incident);
            notificationSenderService.sendNotifications(monitor, incident);
        }
    }

    private void incrementChecksAndChangeTimestamps(Monitor monitor, MonitorStatus currentStatus, long epochSecond) {
        monitor.setLastCheck(epochSecond);
        if (currentStatus == MonitorStatus.UP) {
            monitor.setLastSuccessfulCheck(epochSecond);
            monitor.incrementChecksUp();
        } else if (currentStatus == MonitorStatus.DOWN) {
            monitor.incrementChecksDown();
        }
    }

}
