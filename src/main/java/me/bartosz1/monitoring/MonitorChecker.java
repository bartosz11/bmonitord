package me.bartosz1.monitoring;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.models.enums.MonitorType;
import me.bartosz1.monitoring.repositories.IncidentRepository;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import me.bartosz1.monitoring.services.NotificationSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MonitorChecker implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorChecker.class);
    private final MonitorRepository monitorRepository;
    private final NotificationSenderService notificationSenderService;
    private final IncidentRepository incidentRepository;
    private final ExecutorService executorService;
    private long started;

    public MonitorChecker(MonitorRepository monitorRepository, NotificationSenderService notificationSenderService, IncidentRepository incidentRepository, @Value("${monitoring.check-thread-pool-size:2}") int checkThreadPoolSize) {
        this.monitorRepository = monitorRepository;
        this.notificationSenderService = notificationSenderService;
        this.incidentRepository = incidentRepository;
        this.executorService = Executors.newFixedThreadPool(checkThreadPoolSize);
    }

    @Scheduled(fixedDelay = 60000)
    //might not be a good solution
    @Transactional
    public void checkMonitors() {
        List<Monitor> allMonitors = monitorRepository.findAllMonitors(monitorRepository.findAllMonitors());
        allMonitors.forEach(monitor -> {
            if (!(monitor.getType() == MonitorType.AGENT && started + 300 < Instant.now().getEpochSecond())) {
                if (!monitor.isPaused()) executorService.execute(() -> {
                    MonitorStatus currentStatus = monitor.getType().getCheckProvider().check(monitor);
                    processStatus(monitor, currentStatus);
                    monitorRepository.save(monitor);
                    LOGGER.debug(monitor.getName() + " " + currentStatus);
                });
            }
        });
    }

    //copied from V1
    private void processStatus(Monitor monitor, MonitorStatus currentStatus) {
        incrementChecks(monitor, currentStatus);
        //Get incident sorted by start timestamp descending
        List<Incident> incidents = monitor.getIncidents().stream().sorted(Comparator.comparing(Incident::getStartTimestamp).reversed()).toList();
        long epochSecond = Instant.now().getEpochSecond();
        if (monitor.getLastStatus() != MonitorStatus.UP && currentStatus == MonitorStatus.UP) {
            monitor.setLastStatus(currentStatus);
            if (incidents.isEmpty()) return;
            Incident lastIncident = incidents.get(0);
            if (lastIncident.isOngoing()) {
                lastIncident.setOngoing(false);
                lastIncident.setEndTimestamp(epochSecond);
                lastIncident.setDuration(lastIncident.getEndTimestamp() - lastIncident.getStartTimestamp());
                notificationSenderService.sendNotifications(monitor, incidentRepository.save(lastIncident));
            }
        } else if (monitor.getLastStatus() != MonitorStatus.DOWN && currentStatus == MonitorStatus.DOWN) {
            monitor.setLastStatus(currentStatus);
            if (!incidents.isEmpty()) {
                Incident lastIncident = incidents.get(0);
                if (lastIncident.isOngoing()) return;
            }
            Incident incident = incidentRepository.save(new Incident().setStartTimestamp(epochSecond).setOngoing(true).setMonitor(monitor));
            monitor.getIncidents().add(incident);
            notificationSenderService.sendNotifications(monitor, incident);
        }
        if (currentStatus == MonitorStatus.UP) {
            monitor.setLastSuccessfulCheck(epochSecond);
        }
    }

    private void incrementChecks(Monitor monitor, MonitorStatus currentStatus) {
        if (currentStatus == MonitorStatus.UP) monitor.incrementChecksUp();
        else monitor.incrementChecksDown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.started = Instant.now().getEpochSecond();
    }
}
