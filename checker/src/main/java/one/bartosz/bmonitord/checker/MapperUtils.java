package one.bartosz.bmonitord.checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import one.bartosz.bmonitord.checker.models.WebSocketMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

//Yes, there's a similar class in the orchestrator module, but it's reactive and checker isn't
@Component
public class MapperUtils {

    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperUtils.class);

    public MapperUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature(), true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    public Optional<WebSocketMessageDTO> deserializeMessage(String json) {
        return deserialize(json, WebSocketMessageDTO.class);
    }

    public Optional<String> serializeMessage(WebSocketMessageDTO message) {
        return serialize(message);
    }

    public <T> Optional<T> deserialize(String json, Class<T> type) {
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception thrown while parsing JSON: ", e);
            return Optional.empty();
        }
    }

    public <T> Optional<String> serialize(T message) {
        try {
            return Optional.of(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception thrown while parsing JSON: ", e);
            return Optional.empty();
        }
    }

    public <T> T convertValue(Object from, Class<T> type) {
        return objectMapper.convertValue(from, type);
    }
}
