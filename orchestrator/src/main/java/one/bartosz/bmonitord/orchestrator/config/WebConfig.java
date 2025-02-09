package one.bartosz.bmonitord.orchestrator.config;

import one.bartosz.bmonitord.orchestrator.ws.WebSocketConnectionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebConfig {

    @Bean
    public HandlerMapping handlerMapping(WebSocketConnectionHandler webSocketConnectionHandler) {
        Map<String, WebSocketHandler> handlers = new HashMap<>();
        handlers.put("/orchestrator/ws", webSocketConnectionHandler);
        int order = -1;
        return new SimpleUrlHandlerMapping(handlers, order);
    }
}
