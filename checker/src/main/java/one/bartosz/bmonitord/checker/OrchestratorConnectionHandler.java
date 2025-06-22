package one.bartosz.bmonitord.checker;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import one.bartosz.bmonitord.checker.models.Heartbeat;
import one.bartosz.bmonitord.checker.models.Target;
import one.bartosz.bmonitord.checker.models.WebSocketMessageDTO;
import one.bartosz.bmonitord.checker.providers.CheckProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrchestratorConnectionHandler extends WebSocketListener {

    private final OrchestratorConnectionManager connectionManager;
    private final MapperUtils mapperUtils;
    private final String checkerKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorConnectionHandler.class);
    private int selfId;

    public OrchestratorConnectionHandler(OrchestratorConnectionManager connectionManager, MapperUtils mapperUtils, Config config) {
        this.connectionManager = connectionManager;
        this.mapperUtils = mapperUtils;
        this.checkerKey = config.getKey();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        selfId = 0;
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
        LOGGER.info(text);
        optionalMsg.ifPresent(msg -> {
            String payload = "";
            String[] payloadSplit = new String[0];
            if (msg.getPayload() instanceof String) {
                payload = (String) msg.getPayload();
                payloadSplit = payload.split(" ");
            }
            switch (msg.getType()) {
                case "info":
                    if (payloadSplit[0].equals("auth-successful")) {
                        int id = Integer.parseInt(payloadSplit[1]);
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
                    if (selfId == 0) {
                        LOGGER.warn("Check request was received from the orchestrator, but it won't be fulfilled because the checker hasn't received it's ID (most likely hasn't authenticated yet, can be caused by \"timing\")");
                        break;
                    }
                    //perform the check
                    Target target = mapperUtils.convertValue(msg.getPayload(), Target.class);
                    Heartbeat heartbeat = CheckProvider.getCheckProviderForType(target.getType()).check(target);
                    heartbeat.setCheckerID(selfId);
                    //make the message as json
                    WebSocketMessageDTO messageDTO = new WebSocketMessageDTO().setType("result").setPayload(heartbeat);
                    Optional<String> optionalMessage = mapperUtils.serializeMessage(messageDTO);
                    if (optionalMessage.isEmpty()) break;
                    //send it
                    String s = optionalMessage.get();
                    LOGGER.info(s);
                    webSocket.send(s);
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
