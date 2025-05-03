package one.bartosz.bmonitord.orchestrator;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.WebSocketMessageDTO;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetChecker;
import one.bartosz.bmonitord.common.model.target.TargetStatus;
import one.bartosz.bmonitord.common.repos.*;
import one.bartosz.bmonitord.orchestrator.services.StatusProcessingService;
import one.bartosz.bmonitord.orchestrator.services.WebSocketService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BroadcastTask implements Runnable {

    private final TargetRepository targetRepository;
    private final TargetHTTPInfoRepository targetHTTPInfoRepository;
    private final TargetPingInfoRepository targetPingInfoRepository;
    private final TargetCheckerRepository targetCheckerRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final WebSocketService webSocketService;
    private final ConcurrentHashMap<UUID, StatusProcessingTask> heartbeatQueues;
    private final StatusProcessingService statusProcessingService;


    public BroadcastTask(TargetRepository targetRepository, TargetHTTPInfoRepository targetHTTPInfoRepository, TargetPingInfoRepository targetPingInfoRepository, TargetCheckerRepository targetCheckerRepository, HeartbeatRepository heartbeatRepository, WebSocketService webSocketService, ConcurrentHashMap<UUID, StatusProcessingTask> heartbeatQueues, StatusProcessingService statusProcessingService) {
        this.targetRepository = targetRepository;
        this.targetHTTPInfoRepository = targetHTTPInfoRepository;
        this.targetPingInfoRepository = targetPingInfoRepository;
        this.targetCheckerRepository = targetCheckerRepository;
        this.heartbeatRepository = heartbeatRepository;
        this.webSocketService = webSocketService;
        this.heartbeatQueues = heartbeatQueues;
        this.statusProcessingService = statusProcessingService;
    }

    @Override
    public void run() {
        targetRepository.findAllNotPaused()
                .flatMap(target -> switch (target.getType()) {
                    //Fetch the info relevant to the type (might offload this relation-loading to a different function later)
                    case HTTP -> targetHTTPInfoRepository.findByTargetId(target.getId())
                            .map(httpInfo -> {
                                target.setTargetHTTPInfo(httpInfo);
                                return target;
                            });
                    case PING -> targetPingInfoRepository.findByTargetId(target.getId())
                            .map(pingInfo -> {
                                target.setTargetPingInfo(pingInfo);
                                return target;
                            });
                })
                //join the checkers
                .flatMap(target -> targetCheckerRepository.findAllByTargetId(target.getId())
                        .map(TargetChecker::getCheckerId)
                        .collectList()
                        .map(checkerIds -> Map.entry(target, checkerIds))
                )
                .flatMap(targetWithCheckers -> {
                    Target target = targetWithCheckers.getKey();
                    List<UUID> checkers = targetWithCheckers.getValue();
                    List<UUID> unreachableCheckers = webSocketService.broadcastToSelectedCheckers(new WebSocketMessageDTO().setType("check").setPayload(target), checkers);

                    StatusProcessingTask statusProcessingTask = new StatusProcessingTask().setTarget(target).setCheckers(checkers).setUnreachableCheckers(unreachableCheckers);
                    heartbeatQueues.put(target.getId(), statusProcessingTask);

                    Mono<Void> processing = statusProcessingTask.getHeartbeats().asFlux()
                            .bufferTimeout(checkers.size(), Duration.ofSeconds(15))
                            .take(1)
                            .flatMap(heartbeats -> statusProcessingService.processStatus(heartbeats, statusProcessingTask))
                            .doFinally(signal -> heartbeatQueues.remove(target.getId()))
                            .then();

                    // Emit unknown-status heartbeats
                    Flux<Heartbeat> unknownHeartbeats = Flux.fromIterable(unreachableCheckers)
                            .map(checkerId -> new Heartbeat()
                                    .setCheckerId(checkerId)
                                    .setTargetId(target.getId())
                                    .setStatus(TargetStatus.UNKNOWN)
                                    .setTimestamp(Instant.now())
                            )
                            .doOnNext(statusProcessingTask.getHeartbeats()::tryEmitNext);

                    return unknownHeartbeats.collectList()
                            .flatMapMany(heartbeatRepository::saveAll)
                            .then(processing); // chain the processing
                })
                .then()
                .subscribe();


    }
}
