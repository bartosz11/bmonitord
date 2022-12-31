package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.models.enums.MonitorType;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Instant;

public class SlackNotificationProvider extends NotificationProvider {
    //Copied from DiscordNotificationProvider and adjusted for Slack's formatting
    private static final String UP_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now UP.\", \"text\": \"Time: %timestamp%\\nDuration: %duration%\\nHost: %host%\", \"color\": \"#21cf19\" } ] }";
    private static final String DOWN_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now DOWN.\", \"text\": \"Time: %timestamp%\\nHost: %host%\", \"color\": \"#cf1919\" } ] }";
    private static final String TEST_NOTIFICATION = "{ \"content\": null, \"attachments\": [ {\"text\": \"This is a test notification.\", \"color\": \"#5f5f5f\" } ] }";

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String... args) {
        String slackWebhookURL = args[0];
        if (slackWebhookURL.startsWith("http://") || slackWebhookURL.startsWith("https://")) {
            String replacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
            String thisEmbed = monitor.getLastStatus() == MonitorStatus.UP
                    //if up
                    ? UP_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName())
                    .replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration() * 1000, true, true))
                    .replaceFirst("%timestamp%", super.getDateTimeFormatter().format(Instant.ofEpochSecond(incident.getEndTimestamp())))
                    .replaceFirst("%host%", replacement)
                    //else
                    : DOWN_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName())
                    .replaceFirst("%timestamp%", super.getDateTimeFormatter().format(Instant.ofEpochSecond(incident.getStartTimestamp())))
                    .replaceFirst("%host%", replacement);
            Request req = new Request.Builder().url(slackWebhookURL).post(RequestBody.create(thisEmbed, MediaType.parse("application/json"))).build();
            super.getHttpClient().newCall(req).enqueue(BLANK_CALLBACK);
        }
    }

    @Override
    public void sendTestNotification(String... args) {
        Request req = new Request.Builder().url(args[0]).post(RequestBody.create(TEST_NOTIFICATION, MediaType.parse("application/json"))).build();
        super.getHttpClient().newCall(req).enqueue(BLANK_CALLBACK);
    }
}
