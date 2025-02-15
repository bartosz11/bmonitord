package one.bartosz.bmonitord.orchestrator;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.WebSocketMessageDTO;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetChecker;
import one.bartosz.bmonitord.common.model.target.TargetStatus;
import one.bartosz.bmonitord.common.repos.*;
import one.bartosz.bmonitord.orchestrator.services.WebSocketService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BroadcastTask implements Runnable {

    private final TargetRepository targetRepository;
    private final TargetHTTPInfoRepository targetHTTPInfoRepository;
    private final TargetPingInfoRepository targetPingInfoRepository;
    private final TargetCheckerRepository targetCheckerRepository;
    private final HeartbeatRepository heartbeatRepository;
    private final WebSocketService webSocketService;

    public BroadcastTask(TargetRepository targetRepository, TargetHTTPInfoRepository targetHTTPInfoRepository, TargetPingInfoRepository targetPingInfoRepository, TargetCheckerRepository targetCheckerRepository, HeartbeatRepository heartbeatRepository, WebSocketService webSocketService) {
        this.targetRepository = targetRepository;
        this.targetHTTPInfoRepository = targetHTTPInfoRepository;
        this.targetPingInfoRepository = targetPingInfoRepository;
        this.targetCheckerRepository = targetCheckerRepository;
        this.heartbeatRepository = heartbeatRepository;
        this.webSocketService = webSocketService;
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
                    //send the message and take the checkers who didn't receive it
                    List<UUID> unreachableCheckers = webSocketService.broadcastToSelectedCheckers(new WebSocketMessageDTO().setType("check").setPayload(target), checkers);
                    //create a blank Heartbeat with UNKNOWN status for each of these
                    return Flux.fromIterable(unreachableCheckers)
                            .map(checkerId -> new Heartbeat()
                                    .setCheckerId(checkerId)
                                    .setTargetId(target.getId())
                                    .setStatus(TargetStatus.UNKNOWN)
                                    .setTimestamp(Instant.now())
                            );
                })
                //save the heartbeats
                .collectList()
                .flatMapMany(heartbeatRepository::saveAll)
                .subscribe();

    }
}
