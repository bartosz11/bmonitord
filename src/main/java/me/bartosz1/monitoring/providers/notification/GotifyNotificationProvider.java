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
public class GotifyNotificationProvider extends NotificationProvider {
    //Yes, Gotify notifications contain less info than others, and it'll stay like this I think, push notifications aren't really supposed to be bulky
    private static final String REQUEST_BODY_TEMPLATE = "{ \"priority\": 7, \"title\": \"Monitoring alert\", \"message\": \"%name% is now %status%. Host: %host%\" }";
    private static final String TEST_NOTIFICATION = "{ \"priority\": 4, \"title\": \"Monitoring\", \"message\": \"This is a test notification.\" }";
    private final HttpClient httpClient;

    public GotifyNotificationProvider(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String args) {
        //prepare token and URL
        String[] arg = args.split("\\|");
        String gotifyURL = arg[0].endsWith("/") ? arg[0].substring(0, arg[0].length() - 1) : arg[0];
        String gotifyToken = arg[1];
        //prepare message
        String hostReplacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
        String body = REQUEST_BODY_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%status%", monitor.getLastStatus().name()).replaceFirst("%host%", hostReplacement);
        //send
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(new URI(gotifyURL + "/message")).POST(HttpRequest.BodyPublishers.ofString(body))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("X-Gotify-Key", gotifyToken).build();
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (URISyntaxException ignored) {}
    }

    public void sendTestNotification(String args) {
        String[] arg = args.split("\\|");
        String gotifyURL = arg[0].endsWith("/") ? arg[0].substring(0, arg[0].length() - 1) : arg[0];
        String gotifyToken = arg[1];
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(new URI(gotifyURL + "/message")).POST(HttpRequest.BodyPublishers.ofString(TEST_NOTIFICATION))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("X-Gotify-Key", gotifyToken).build();
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (URISyntaxException ignored) {}
    }
}
