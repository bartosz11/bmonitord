package one.bartosz.bmonitord.orchestrator.services;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.Incident;
import one.bartosz.bmonitord.common.model.WebSocketMessageDTO;
import one.bartosz.bmonitord.common.model.alarm.Alarm;
import one.bartosz.bmonitord.common.model.alarm.AlarmNotification;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetStatus;
import one.bartosz.bmonitord.common.repos.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class StatusProcessingService {

    private final TargetRepository targetRepository;
    private final AlarmRepository alarmRepository;
    private final IncidentRepository incidentRepository;
    private final AlarmNotificationRepository alarmNotificationRepository;
    private  final NotificationRepository notificationRepository;

    public StatusProcessingService(TargetRepository targetRepository, AlarmRepository alarmRepository, IncidentRepository incidentRepository, AlarmNotificationRepository alarmNotificationRepository, NotificationRepository notificationRepository) {
        this.targetRepository = targetRepository;
        this.alarmRepository = alarmRepository;
        this.incidentRepository = incidentRepository;
        this.alarmNotificationRepository = alarmNotificationRepository;
        this.notificationRepository = notificationRepository;
    }

    public Mono<WebSocketMessageDTO> processStatus(Mono<Heartbeat> heartbeatMono) {
        return getHeartbeatsTarget(heartbeatMono)
                .flatMap(hb -> {
                    Target target = hb.getTarget();
                    if (hb.getStatus() == TargetStatus.UP) { // If up, just reset used retries count back to 0 and go back to the chain
                        return targetRepository.save(target.setUsedRetries(0)).thenReturn(hb);
                    }
                    target.incrementUsedRetries();
                    return targetRepository.save(target) // Save the just-incremented retry amount, if it is lower or equal to max don't process this check further, otherwise continue
                            .flatMap(savedTarget -> savedTarget.getUsedRetries() <= savedTarget.getMaxRetries() ? Mono.empty() : Mono.just(hb));
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

                .map(heartbeat -> new WebSocketMessageDTO().setType("info").setPayload("check-ok"));
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

