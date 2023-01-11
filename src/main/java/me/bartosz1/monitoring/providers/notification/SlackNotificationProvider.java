package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.models.enums.MonitorType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class SlackNotificationProvider extends NotificationProvider {
    //Copied from DiscordNotificationProvider and adjusted for Slack's formatting
    private static final String UP_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now UP.\", \"text\": \"Time: %timestamp%\\nDuration: %duration%\\nHost: %host%\", \"color\": \"#21cf19\" } ] }";
    private static final String DOWN_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now DOWN.\", \"text\": \"Time: %timestamp%\\nHost: %host%\", \"color\": \"#cf1919\" } ] }";
    private static final String TEST_NOTIFICATION = "{ \"content\": null, \"attachments\": [ {\"text\": \"This is a test notification.\", \"color\": \"#5f5f5f\" } ] }";
    private final DateTimeFormatter dateTimeFormatter;
    private final OkHttpClient httpClient;

    public SlackNotificationProvider(DateTimeFormatter dateTimeFormatter, OkHttpClient httpClient) {
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
            Request req = new Request.Builder().url(args).post(RequestBody.create(thisEmbed, MediaType.parse("application/json"))).build();
            httpClient.newCall(req).enqueue(BLANK_CALLBACK);
        }
    }

    @Override
    public void sendTestNotification(String args) {
        Request req = new Request.Builder().url(args).post(RequestBody.create(TEST_NOTIFICATION, MediaType.parse("application/json"))).build();
        httpClient.newCall(req).enqueue(BLANK_CALLBACK);
    }
}
