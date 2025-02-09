package one.bartosz.bmonitord.orchestrator.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.bartosz.bmonitord.orchestrator.model.WebSocketMessageDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class WebSocketUtils {

    private final ObjectMapper objectMapper;

    public WebSocketUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<WebSocketMessageDTO> deserializeMessage(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, WebSocketMessageDTO.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Invalid JSON format", e));
        }
    }

    public Mono<String> serializeMessage(WebSocketMessageDTO response) {
        try {
            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Serialization error", e));
        }
    }
}
