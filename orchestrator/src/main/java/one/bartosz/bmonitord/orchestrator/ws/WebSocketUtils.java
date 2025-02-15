package one.bartosz.bmonitord.orchestrator.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.bartosz.bmonitord.common.model.WebSocketMessageDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class WebSocketUtils {

    private final ObjectMapper objectMapper;

    public WebSocketUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<WebSocketMessageDTO> deserializeMessage(String json) {
        return deserialize(json, WebSocketMessageDTO.class);
    }

    public Mono<String> serializeMessage(WebSocketMessageDTO response) {
        return serialize(response);
    }

    public <T> Mono<T> deserialize(String json, Class<T> type) {
        try {
            return Mono.just(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Invalid JSON format", e));
        }
    }

    public <T> Mono<String> serialize(T message) {
        try {
            return Mono.just(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Serialization error", e));
        }
    }
}
