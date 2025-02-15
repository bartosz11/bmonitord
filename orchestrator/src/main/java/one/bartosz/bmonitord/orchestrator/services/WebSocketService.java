package one.bartosz.bmonitord.orchestrator.services;

import jakarta.annotation.PreDestroy;
import one.bartosz.bmonitord.common.model.Checker;
import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.Orchestrator;
import one.bartosz.bmonitord.common.model.WebSocketMessageDTO;
import one.bartosz.bmonitord.common.repos.CheckerRepository;
import one.bartosz.bmonitord.common.repos.OrchestratorRepository;
import one.bartosz.bmonitord.common.repos.TargetRepository;
import one.bartosz.bmonitord.orchestrator.ws.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class WebSocketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketService.class);
    private final LeaderElectionService leaderElectionService;
    private final OrchestratorRepository orchestratorRepository;
    private final CheckerRepository checkerRepository;
    private final HashMap<UUID, WebSocketSession> checkerSessions = new HashMap<>();
    private final WebSocketUtils webSocketUtils;
    private final StatusProcessingService statusProcessingService;

    public WebSocketService(LeaderElectionService leaderElectionService, OrchestratorRepository orchestratorRepository, CheckerRepository checkerRepository, WebSocketUtils webSocketUtils, StatusProcessingService statusProcessingService) {
        this.leaderElectionService = leaderElectionService;
        this.orchestratorRepository = orchestratorRepository;
        this.checkerRepository = checkerRepository;
        this.webSocketUtils = webSocketUtils;
        this.statusProcessingService = statusProcessingService;
    }

    public Mono<WebSocketMessageDTO> processWebSocketMessage(WebSocketMessageDTO wsMessage, WebSocketSession session) {
        //Shouldn't be null on server but IntellIJ being IntellIJ
        String ip = Objects.requireNonNull(session.getHandshakeInfo().getRemoteAddress()).getAddress().getHostAddress();
        if (!leaderElectionService.isLeader()) {
            //Send back a "go to this guy" message
            LOGGER.debug("{} -> /orchestrator/ws -> connection rejected, not leader", ip);
            return orchestratorRepository.findFirstByLeader(true).defaultIfEmpty(new Orchestrator()).map(Orchestrator::getHost)
                    .flatMap(host -> Mono.just(new WebSocketMessageDTO().setType("end").setPayload("not-leader " + host)));
        }

        return switch (wsMessage.getType()) {
            case "auth" -> checkerRepository.findFirstByKey(String.valueOf(wsMessage.getPayload()))
                    .map(Checker::getId).map(id -> {
                        checkerSessions.put(id, session);
                        LOGGER.debug("{} -> /orchestrator/ws -> connection successful, checker ID {}", ip, id);
                        return new WebSocketMessageDTO().setType("info").setPayload("auth-successful " + id);
                    })
                    .switchIfEmpty(Mono.just(new WebSocketMessageDTO().setType("end").setPayload("invalid auth key")));
            case "result" -> {
                if (!checkerSessions.containsValue(session))
                    yield Mono.just(new WebSocketMessageDTO().setType("error").setPayload("unauthorized"));

                yield webSocketUtils.deserialize((String) wsMessage.getPayload(), Heartbeat.class).map(Mono::just).flatMap(statusProcessingService::processStatus);
            }
            default -> Mono.just(new WebSocketMessageDTO().setType("error").setPayload("invalid message type"));
        };
    }

    public List<UUID> broadcastToSelectedCheckers(WebSocketMessageDTO message, List<UUID> selectedCheckers) {
        for (Map.Entry<UUID, WebSocketSession> entry : checkerSessions.entrySet()) {
            if (selectedCheckers.contains(entry.getKey())) {
                WebSocketSession session = entry.getValue();
                webSocketUtils.serializeMessage(message)
                        .map(session::textMessage)
                        .map(Mono::just)
                        .flatMap(session::send).subscribe(null, error -> checkerSessions.remove(entry.getKey()));
            }
        }
        //Returns the list of checker ID's that couldn't be reached ()
        return selectedCheckers.stream().filter(id -> !checkerSessions.containsKey(id)).toList();
    }

    public void broadcastToAllCheckers(WebSocketMessageDTO message) {
        for (Map.Entry<UUID, WebSocketSession> entry : checkerSessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            webSocketUtils.serializeMessage(message)
                    .map(session::textMessage)
                    .map(Mono::just)
                    .flatMap(session::send).subscribe(null, error -> checkerSessions.remove(entry.getKey()));
        }
    }

    @PreDestroy
    //Well, not really graceful
    private void gracefulShutdown() {
//          TODO: probably needs a "reactive redo"
        checkerSessions.values().forEach(session -> {
//            This code makes sense I guess
            if (session.isOpen()) session.close(CloseStatus.GOING_AWAY).subscribe();
        });
    }

}
