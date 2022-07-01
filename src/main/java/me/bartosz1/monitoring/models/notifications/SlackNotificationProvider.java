package me.bartosz1.monitoring.models.notifications;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.MonitorStatus;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class SlackNotificationProvider extends NotificationProvider {
    //Copied from DiscordNotificationProvider and adjusted for Slack's formatting
    private static final String UP_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now UP.\", \"text\": \"Time: %timestamp%\\nDuration: %duration%\\nHost: %host%\", \"color\": \"#21cf19\" } ] }";
    private static final String DOWN_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now DOWN.\", \"text\": \"Time: %timestamp%\\nHost: %host%\", \"color\": \"#cf1919\" } ] }";
    @Autowired
    private DateTimeFormatter dateTimeFormatter;

    public void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String slackWebhookURL = contactList.getSlackWebhookURL();
        if (slackWebhookURL.startsWith("http://") || slackWebhookURL.startsWith("https://")) {
            String thisEmbed;
            if (monitor.getLastStatus() == MonitorStatus.UP) {
                thisEmbed = UP_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration() * 1000, true, true));
                if (monitor.getType().equalsIgnoreCase("agent"))
                    thisEmbed = thisEmbed.replaceFirst("%host%", "Server Agent");
                else
                    thisEmbed = thisEmbed.replaceFirst("%host%", monitor.getHost());
                thisEmbed = thisEmbed.replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getEndTimestamp())));
            } else {
                thisEmbed = DOWN_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName());
                if (monitor.getType().equalsIgnoreCase("agent"))
                    thisEmbed = thisEmbed.replaceFirst("%host%", "Server Agent");
                else
                    thisEmbed = thisEmbed.replaceFirst("%host%", monitor.getHost());
                thisEmbed = thisEmbed.replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getStartTimestamp())));
            }
            Request req = new Request.Builder().url(slackWebhookURL).post(RequestBody.create(thisEmbed, MediaType.parse("application/json"))).build();
            NotificationProvider.okHttpClient.newCall(req).enqueue(NotificationProvider.BLANK_HTTP_CALLBACK);
        }
    }

}
