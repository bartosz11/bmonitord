package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class PushbulletNotificationProvider extends NotificationProvider {

    private static final String NOTIFICATION_TEMPLATE = "{\"type\":\"note\",\"title\":\"Monitoring alert\",\"body\":\"%name% is now %status%. Host: %host%\"}";
    private static final String TEST_NOTIFICATION = "{\"type\":\"note\",\"title\":\"Monitoring\",\"body\":\"This is a test notification.\"}";
    private final HttpClient httpClient;

    public PushbulletNotificationProvider(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String args) {
        String hostReplacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
        String body = NOTIFICATION_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%status%", monitor.getLastStatus().name()).replaceFirst("%host%", hostReplacement);
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(new URI("https://api.pushbullet.com/v2/pushes"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Access-Token", args).build();
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (URISyntaxException ignored) {}
    }

    @Override
    public void sendTestNotification(String args) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(new URI("https://api.pushbullet.com/v2/pushes"))
                    .POST(HttpRequest.BodyPublishers.ofString(TEST_NOTIFICATION))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Access-Token", args).build();
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (URISyntaxException ignored) {}
    }
}
