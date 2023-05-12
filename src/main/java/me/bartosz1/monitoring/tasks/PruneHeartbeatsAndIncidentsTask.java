package me.bartosz1.monitoring.tasks;

import jakarta.transaction.Transactional;
import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.repositories.HeartbeatRepository;
import me.bartosz1.monitoring.repositories.IncidentRepository;
import me.bartosz1.monitoring.repositories.MonitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Component
public class PruneHeartbeatsAndIncidentsTask {

    private final HeartbeatRepository heartbeatRepository;
    private final IncidentRepository incidentRepository;
    private final MonitorRepository monitorRepository;
    private final int pruneDays;
    private final boolean runGC;
    private static final Logger LOGGER = LoggerFactory.getLogger(PruneHeartbeatsAndIncidentsTask.class);

    public PruneHeartbeatsAndIncidentsTask(HeartbeatRepository heartbeatRepository, IncidentRepository incidentRepository, MonitorRepository monitorRepository, @Value("${monitoring.prune.age:14}") int pruneDays, @Value("${monitoring.run-gc-after-tasks:false}") boolean runGC) {
        this.heartbeatRepository = heartbeatRepository;
        this.incidentRepository = incidentRepository;
        this.monitorRepository = monitorRepository;
        this.pruneDays = pruneDays;
        this.runGC = runGC;
    }

    @Scheduled(fixedDelayString = "${monitoring.prune.delay:86400}", timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void cleanup() {
        //not sure about unresponsiveness
        LOGGER.info("Starting prune of old heartbeats and incidents, app might become unresponsive. NEXT MESSAGE FROM THIS TASK DOESN'T INDICATE IT'S DONE");
        long min = Instant.now().getEpochSecond() - pruneDays * 86400L;
        LOGGER.debug("Using " + min + " as min timestamp");
        Set<Monitor> bulkUpdateMonitors = new HashSet<>();
        //delete all orphans i guess
        heartbeatRepository.deleteAllByMonitorIsNull();
        incidentRepository.deleteAllByMonitorIsNull();
        Iterable<Heartbeat> targetHeartbeats = heartbeatRepository.findAllByTimestampLessThan(min);
        List<Incident> targetIncidents = StreamSupport.stream(incidentRepository.findAllByStartTimestampLessThan(min).spliterator(), false).filter(incident -> !incident.isOngoing()).toList();
        targetHeartbeats.forEach(hb -> {
            //how to spam debug logs 101
            LOGGER.debug("Processing heartbeat ID " + hb.getId() + " monitor " + hb.getMonitor().getId());
            //unbind from monitor
            Monitor monitor = hb.getMonitor();
            monitor.getHeartbeats().remove(hb);
            bulkUpdateMonitors.add(monitor);
        });
        targetIncidents.forEach(incident -> {
            LOGGER.debug("Processing incident ID " + incident.getId() + " monitor " + incident.getMonitor().getId());
            Monitor monitor = incident.getMonitor();
            monitor.getIncidents().remove(incident);
            bulkUpdateMonitors.add(monitor);
        });
        //monitor cascades all operation to incident and heartbeat objects
        monitorRepository.saveAll(bulkUpdateMonitors);
        LOGGER.info("At least " + targetHeartbeats.spliterator().getExactSizeIfKnown() + " heartbeats, " + targetIncidents.size() + " incidents affected");
        if (runGC) {
            LOGGER.info("Running JVM garbage collector.");
            System.gc();
        }
    }
}
