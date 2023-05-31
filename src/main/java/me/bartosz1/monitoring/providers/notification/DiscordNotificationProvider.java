package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.models.enums.MonitorType;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class DiscordNotificationProvider extends NotificationProvider {

    private static final String UP_EMBED_TEMPLATE = "{ \"content\": null, \"embeds\": [ { \"title\": \"%name% is now UP.\", \"description\": \"Time: %timestamp%\\nDuration: %duration%\\nHost: %host%\", \"color\": 2215705 }], \"attachments\": [] }";
    private static final String DOWN_EMBED_TEMPLATE = "{ \"content\": null, \"embeds\": [ { \"title\": \"%name% is now DOWN.\", \"description\": \"Time: %timestamp%\\nHost: %host%\", \"color\": 13572377 } ], \"attachments\": [] }";
    private static final String TEST_NOTIFICATION = "{ \"content\": null, \"embeds\": [ {\"description\": \"This is a test notification.\", \"color\" :6250335 } ], \"attachments\": [] }";
    private final DateTimeFormatter dateTimeFormatter;
    private final HttpClient httpClient;

    public DiscordNotificationProvider(DateTimeFormatter dateTimeFormatter, HttpClient httpClient) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.httpClient = httpClient;
    }

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String args) {
        if (args.startsWith("http://") || args.startsWith("https://")) {
            String replacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
            String thisEmbed = monitor.getLastStatus() == MonitorStatus.UP
                    //if up
                    ? UP_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName())
                    .replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration() * 1000, true, true))
                    .replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getEndTimestamp())))
                    .replaceFirst("%host%", replacement)
                    //else
                    : DOWN_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName())
                    .replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getStartTimestamp())))
                    .replaceFirst("%host%", replacement);
            try {
                HttpRequest req = HttpRequest.newBuilder().uri(new URI(args)).POST(HttpRequest.BodyPublishers.ofString(thisEmbed)).header("Content-Type", "application/json").build();
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
            } catch (URISyntaxException ignored) {}
        }
    }

    @Override
    public void sendTestNotification(String args) {
        if (args.startsWith("http://") || args.startsWith("https://")) {
            try {
                HttpRequest req = HttpRequest.newBuilder().uri(new URI(args)).POST(HttpRequest.BodyPublishers.ofString(TEST_NOTIFICATION)).header("Content-Type", "application/json").build();
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
            } catch (URISyntaxException ignored) {}
        }
    }
}
