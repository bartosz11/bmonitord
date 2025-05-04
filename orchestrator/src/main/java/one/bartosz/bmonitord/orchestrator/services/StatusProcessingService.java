package one.bartosz.bmonitord.orchestrator.services;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.Incident;
import one.bartosz.bmonitord.common.model.alarm.Alarm;
import one.bartosz.bmonitord.common.model.alarm.AlarmNotification;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetStatus;
import one.bartosz.bmonitord.common.repos.*;
import one.bartosz.bmonitord.orchestrator.StatusProcessingTask;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class StatusProcessingService {

    private final TargetRepository targetRepository;
    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;
    private final AlarmNotificationRepository alarmNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final HeartbeatRepository heartbeatRepository;


    public StatusProcessingService(TargetRepository targetRepository, AlarmRepository alarmRepository, IncidentRepository incidentRepository, AlarmNotificationRepository alarmNotificationRepository, NotificationRepository notificationRepository, HeartbeatRepository heartbeatRepository) {
        this.targetRepository = targetRepository;
        this.alarmRepository = alarmRepository;
        this.incidentRepository = incidentRepository;
        this.alarmNotificationRepository = alarmNotificationRepository;
        this.notificationRepository = notificationRepository;
        this.heartbeatRepository = heartbeatRepository;
    }

    //This function is called by the "queue" that's created in BroadcastTask to process that "queue"
    public Mono<Void> processStatus(List<Heartbeat> heartbeats, StatusProcessingTask statusProcessingTask) {
        List<Heartbeat> missingHeartbeats = statusProcessingTask.getCheckers().stream()
                //For the checkers that were reachable when the task was broadcast but haven't sent anything in time
                .filter(checker -> !(statusProcessingTask.getCheckersDone().contains(checker) || statusProcessingTask.getUnreachableCheckers().contains(checker)))
                .map(checker -> new Heartbeat().setCheckerId(checker).setTargetId(statusProcessingTask.getTarget().getId()).setStatus(TargetStatus.UNKNOWN).setTimestamp(Instant.now()))
                .toList();

        heartbeats.addAll(missingHeartbeats);

        List<Heartbeat> knownStatuses = heartbeats.stream().filter(heartbeat -> heartbeat.getStatus() != TargetStatus.UNKNOWN).sorted((Comparator.comparing(Heartbeat::getTimestamp))).toList();
        //We can stop the processing there since there's no data to justify changing the other data
        if (knownStatuses.isEmpty()) return Mono.empty();
        //find the first DOWN, use it as decisive, if not found take the first element as decisive (it's UP)
        statusProcessingTask.setDecisiveHeartbeat(
                knownStatuses.stream()
                        .filter(hb -> hb.getStatus() == TargetStatus.DOWN)
                        .findFirst()
                        .orElse(knownStatuses.getFirst())
        );

        return getHeartbeatsTarget(Mono.just(statusProcessingTask.getDecisiveHeartbeat()))
                .flatMap(hb -> {
                    Target target = hb.getTarget();
                    target.setLastCheck(hb.getTimestamp());
                    if (hb.getStatus() == TargetStatus.UP) {
                        target.incrementChecks(TargetStatus.UP).setUsedRetries(0).setLastStatus(TargetStatus.UP);
                        return targetRepository.save(target).thenReturn(hb);
                    }

                    target.incrementUsedRetries();
                    if (target.getUsedRetries() > target.getMaxRetries()) {
                        target.incrementChecks(TargetStatus.DOWN).setLastStatus(TargetStatus.DOWN);
                    }
                    //Save the used retries count everytime, but continue the chain only if it has been exceeded
                    return targetRepository.save(target).flatMap(saved -> saved.getUsedRetries() <= saved.getMaxRetries() ? Mono.empty() : Mono.just(hb));
                })
                .flatMap(this::getTargetDetails) // Fetch alarms and incidents
                .flatMap(heartbeat -> {
                    //if the status hasn't changed we don't need any incident processing
                    if (heartbeat.getStatus() == heartbeat.getTarget().getLastStatus()) return Mono.just(heartbeat);
                    List<Incident> incidents = heartbeat.getTarget().getIncidents();
                    if (heartbeat.getStatus() == TargetStatus.UP) {
                        //Status has changed from DOWN to UP, there had to be an incident created beforehand therefore no checks if there are any before retrieving
                        Incident lastIncident = incidents.getFirst();
                        lastIncident.setOngoing(false);
                        lastIncident.setEnd(heartbeat.getTimestamp());
                        lastIncident.setDuration(Duration.between(lastIncident.getStart(), lastIncident.getEnd()));
                        return incidentRepository.save(lastIncident).thenReturn(heartbeat);
                    } else { // We can safely use "else" for DOWN case, because check providers aren't supposed to return UNKNOWN
                        //Status has changed from UP to DOWN, we have to create a new incident
                        Incident incident = new Incident().setStart(heartbeat.getTimestamp()).setOngoing(true).setTargetId(heartbeat.getTargetId());
                        return incidentRepository.save(incident).thenReturn(heartbeat);
                    }
                })
                .flatMap(heartbeat -> {
                    List<Alarm> alarms = heartbeat.getTarget().getAlarms();
                    return Flux.fromIterable(alarms).map(alarm -> {
                        switch (alarm.getType()) {
                            case UNAVAILABLE -> {
                                if (alarm.isActive() && heartbeat.getStatus() == TargetStatus.UP) { // Service is back
                                    alarm.setActive(false);
                                    //todo: send UP notifications if not muted
                                }
                                if (!alarm.isActive() && heartbeat.getStatus() == TargetStatus.DOWN) { // Service has become unavailable
                                    alarm.setActive(true);
                                    //todo: send DOWN notifications if not muted
                                }
                            }
                            case THRESHOLD -> {
                                if (alarm.isActive() && alarm.getThresholdField().getValueFromHeartbeat(heartbeat) < alarm.getThreshold()) {
                                    alarm.setActive(false);
                                    //todo: send threshold no longer exceeded notifications
                                }
                                if (!alarm.isActive() && alarm.getThresholdField().getValueFromHeartbeat(heartbeat) > alarm.getThreshold()) {
                                    alarm.setActive(true);
                                    //TODO: send threshold exceeded notifications
                                }
                            }
                        }
                        return alarm;
                    }).collectList().flatMapMany(alarmRepository::saveAll).then(Mono.just(heartbeat));
                })
                .thenMany(heartbeatRepository.saveAll(heartbeats)).then();
    }

    private Mono<Heartbeat> getHeartbeatsTarget(Mono<Heartbeat> heartbeatMono) {
        return heartbeatMono.flatMap(hb ->
                targetRepository.findById(hb.getTargetId()).map(hb::setTarget)
        );
    }

    private Mono<Heartbeat> getTargetDetails(Heartbeat heartbeat) {
        return Mono.just(heartbeat).flatMap(hb -> {
            Target target = hb.getTarget();

            Mono<List<Incident>> incidentsMono = incidentRepository
                    .findAllByTargetIdOrderByStartDesc(target.getId()).collectList()
                    .defaultIfEmpty(Collections.emptyList());

            Mono<List<Alarm>> alarmsMono = alarmRepository
                    .findAllByTargetId(target.getId()).collectList()
                    .defaultIfEmpty(Collections.emptyList());

            return Mono.zip(incidentsMono, alarmsMono)
                    .flatMap(tuple -> {
                        target.setIncidents(tuple.getT1());

                        List<Alarm> alarms = tuple.getT2();
                        target.setAlarms(alarms);
                        //Fetch notifications bound to alarms and assign them
                        return Flux.fromIterable(alarms)
                                .flatMap(alarm -> alarmNotificationRepository.findAllByAlarmId(alarm.getId())
                                        .map(AlarmNotification::getNotificationId)
                                        .collectList()
                                        .flatMap(notificationIds -> notificationRepository.findAllById(notificationIds).collectList())
                                        .doOnNext(alarm::setNotifications)
                                )
                                .then(Mono.just(hb));
                    });
        });
    }

}

