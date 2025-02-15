package one.bartosz.bmonitord.checker;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import one.bartosz.bmonitord.checker.providers.CheckProvider;
import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.WebSocketMessageDTO;
import one.bartosz.bmonitord.common.model.target.Target;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrchestratorConnectionHandler extends WebSocketListener {

    private final OrchestratorConnectionManager connectionManager;
    private final MapperUtils mapperUtils;
    private final String checkerKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorConnectionHandler.class);
    private UUID selfId;

    public OrchestratorConnectionHandler(OrchestratorConnectionManager connectionManager, MapperUtils mapperUtils, @Value("${bmonitord.checker.key}") String checkerKey) {
        this.connectionManager = connectionManager;
        this.mapperUtils = mapperUtils;
        this.checkerKey = checkerKey;
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        selfId = null;
        String host = webSocket.request().url().host();
        LOGGER.info("WS connection to orchestrator {} opened - attempting authentication.", host);

        WebSocketMessageDTO authMsg = new WebSocketMessageDTO().setType("auth").setPayload(checkerKey);
        Optional<String> serialized = mapperUtils.serializeMessage(authMsg);
        serialized.ifPresent(webSocket::send);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        String host = webSocket.request().url().host();
        Optional<WebSocketMessageDTO> optionalMsg = mapperUtils.deserializeMessage(text);
        optionalMsg.ifPresent(msg -> {
            String payload = (String) msg.getPayload();
            String[] payloadSplit = payload.split(" ");
            switch (msg.getType()) {
                case "info":
                    if (payloadSplit[0].equals("auth-successful")) {
                        UUID id = UUID.fromString(payloadSplit[1]);
                        LOGGER.info("Successfully authenticated! Orchestrator: {}, checker ID: {}", host, id);
                        this.selfId = id;
                    }
                    break;
                case "end":
                    if (payloadSplit[0].equals("not-leader")) {
                        String nextHost = payloadSplit[1];
                        if (nextHost.startsWith("ws://") || nextHost.startsWith("wss://")) {
                            connectionManager.setNextOrchestrator(nextHost);
                            LOGGER.info("Orchestrator {} is not the leader. Will attempt connecting to {}, as it was suggested.", host, nextHost);
                        }
                    } else if (payload.equals("invalid auth key")) {
                        LOGGER.error("Auth key was considered invalid by orchestrator {}. Checker will connect to the next orchestrator.", host);
                    }
                    break;
                case "check":
                    if (selfId == null) {
                        LOGGER.warn("Check request was received from the orchestrator, but it won't be fulfilled because the checker hasn't received it's ID (most likely hasn't authenticated yet, can be caused by \"timing\")");
                        break;
                    }
                    //perform the check
                    Optional<Target> optionalTarget = mapperUtils.deserialize(payload, Target.class);
                    if (optionalTarget.isEmpty()) break;
                    Target target = optionalTarget.get();
                    Heartbeat heartbeat = CheckProvider.getCheckProviderForType(target.getType()).check(target);
                    heartbeat.setCheckerId(selfId);
                    //convert the heartbeat into json string
                    Optional<String> optionalPayload = mapperUtils.serialize(heartbeat);
                    if (optionalPayload.isEmpty()) break;
                    //make the message as json
                    WebSocketMessageDTO messageDTO = new WebSocketMessageDTO().setType("result").setPayload(optionalPayload.get());
                    Optional<String> optionalMessage = mapperUtils.serializeMessage(messageDTO);
                    if (optionalMessage.isEmpty()) break;
                    //send it
                    webSocket.send(optionalMessage.get());
                    break;
                case "error":
                    //These errors shouldn't even happen as we're not exceeding the "expected use case"
                    LOGGER.error("A message with type \"error\" was sent by the orchestrator. Content: {}", payload);
                    break;
            }
        });
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        //Maybe this should be done in onClosing
        String host = webSocket.request().url().host();
        LOGGER.info("WS connection to orchestrator {} closed - code: {}, reason: {}. Attempting connection to next orchestrator.", host, code, reason);
        connectionManager.connect();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        String host = webSocket.request().url().host();
        LOGGER.error("Error occurred in WS connection to orchestrator {}, attempting connection to next orchestrator.", host, t);
        connectionManager.connect();
    }
}
