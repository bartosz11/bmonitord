package one.bartosz.bmonitord.orchestrator.ws;

import one.bartosz.bmonitord.orchestrator.services.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class WebSocketConnectionHandler implements WebSocketHandler {

    private final WebSocketService webSocketService;
    private final WebSocketUtils webSocketUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConnectionHandler.class);

    public WebSocketConnectionHandler(WebSocketService webSocketService, WebSocketUtils webSocketUtils) {
        this.webSocketService = webSocketService;
        this.webSocketUtils = webSocketUtils;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
//        TODO: probably add some task that will disconnect WS's if they don't send anything in like ~15 seconds?
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(webSocketUtils::deserializeMessage) // parse the message into JSON
                .flatMap(message -> webSocketService.processWebSocketMessage(message, session)) // do the stuff in our service
                .flatMap(message -> {
                    Mono<Void> send = webSocketUtils.serializeMessage(message)
                            .map(session::textMessage)
                            .map(Mono::just)
                            .flatMap(session::send); // Send the message

                    //If the type is "end", also close the connection
                    if (message.getType().equals("end")) {
                        return send.then(session.close()); // Ensure closing happens AFTER sending
                    }

                    return send;
                })
                .then(); //send it
    }


}
