package me.bartosz1.monitoring.models.notifications;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.MonitorStatus;
import okhttp3.*;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SlackNotificationProvider implements NotificationProvider {
    //Copied from DiscordNotificationProvider and adjusted for Slack's formatting
    private static final String UP_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now UP.\", \"text\": \"Duration: %duration%\\nHost: %host%\", \"color\": \"#21cf19\", \"ts\": %d } ] }";
    private static final String DOWN_EMBED_TEMPLATE = "{ \"content\": null, \"attachments\": [ { \"title\": \"%name% is now DOWN.\", \"text\": \"Host: %host%\", \"color\": \"#cf1919\", \"ts\": %d } ] }";
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    public static void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String slackWebhookURL = contactList.getSlackWebhookURL();
        if (slackWebhookURL.startsWith("http://") || slackWebhookURL.startsWith("https://")) {
            String thisEmbed;
            if (monitor.getLastStatus() == MonitorStatus.UP) {
                thisEmbed = UP_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration() * 1000, true, true));
                if (monitor.getType().equalsIgnoreCase("agent"))
                    thisEmbed = thisEmbed.replaceFirst("%host%", "Server Agent");
                else
                    thisEmbed = thisEmbed.replaceFirst("%host%", monitor.getHost());
                thisEmbed = String.format(thisEmbed, incident.getEndTimestamp());
            } else {
                thisEmbed = DOWN_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName());
                if (monitor.getType().equalsIgnoreCase("agent"))
                    thisEmbed = thisEmbed.replaceFirst("%host%", "Server Agent");
                else
                    thisEmbed = thisEmbed.replaceFirst("%host%", monitor.getHost());
                thisEmbed = String.format(thisEmbed, incident.getStartTimestamp());
            }
            Request req = new Request.Builder().url(slackWebhookURL).post(RequestBody.create(thisEmbed, MediaType.parse("application/json"))).build();
            okHttpClient.newCall(req).enqueue(BLANK_HTTP_CALLBACK);
        }
    }

    private static final Callback BLANK_HTTP_CALLBACK = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            response.body().close();
            response.close();
        }
    };
}
