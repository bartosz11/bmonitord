package me.bartosz1.monitoring.providers.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

@Component
public class GenericWebhookNotificationProvider extends NotificationProvider {

    private static final String TEST_NOTIFICATION = "{\"event\": \"test-notification\"}";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GenericWebhookNotificationProvider(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String args) {
        if (args.startsWith("http://") || args.startsWith("https://")) {
            HashMap<String, Object> bodyContent = new HashMap<>();
            bodyContent.put("event", "status-change");
            bodyContent.put("status", monitor.getLastStatus().name().toLowerCase());
            bodyContent.put("monitor", monitor);
            bodyContent.put("incident", incident);
            try {
                String bodyAsString = objectMapper.writeValueAsString(bodyContent);
                HttpRequest req = HttpRequest.newBuilder().uri(new URI(args)).POST(HttpRequest.BodyPublishers.ofString(bodyAsString)).header("Content-Type", "application/json").build();
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
            } catch (JsonProcessingException | URISyntaxException ignored) {}
        }
    }

    @Override
    public void sendTestNotification(String args) {
        if (args.startsWith("http://") || args.startsWith("https://")) {
            try {
                String bodyAsString = objectMapper.writeValueAsString(TEST_NOTIFICATION);
                HttpRequest req = HttpRequest.newBuilder().uri(new URI(args)).POST(HttpRequest.BodyPublishers.ofString(bodyAsString)).header("Content-Type", "application/json").build();
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
            } catch (JsonProcessingException | URISyntaxException ignored) {}
        }
    }
}
