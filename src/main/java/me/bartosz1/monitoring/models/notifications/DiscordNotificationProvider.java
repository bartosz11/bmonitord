package me.bartosz1.monitoring.models.notifications;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.MonitorStatus;
import okhttp3.*;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DiscordNotificationProvider implements NotificationProvider {

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    private static final String UP_EMBED_TEMPLATE = "{ \"content\": null, \"embeds\": [ { \"title\": \"%name% is now UP.\", \"description\": \"Duration: %duration%\\nHost: %host%\", \"color\": 2215705, \"timestamp\": \"%timestamp%\" }], \"attachments\": [] }";
    private static final String DOWN_EMBED_TEMPLATE = "{ \"content\": null, \"embeds\": [ { \"title\": \"%name% is now DOWN.\", \"description\": \"Host: %host%\", \"color\": 13572377, \"timestamp\": \"%timestamp%\" } ], \"attachments\": [] }";

    public static void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String discordWebhookURL = contactList.getDiscordWebhookURL();
        if (discordWebhookURL.startsWith("http://") || discordWebhookURL.startsWith("https://")) {
            String thisEmbed;
            if (monitor.getLastStatus()== MonitorStatus.UP) {
                thisEmbed = UP_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration()*1000, true, true));
                if (monitor.getType().equalsIgnoreCase("agent")) thisEmbed = thisEmbed.replaceFirst("%host%", "Server Agent");
                else thisEmbed = thisEmbed.replaceFirst("%host%", "["+monitor.getHost()+"]("+monitor.getHost()+")");
                thisEmbed = thisEmbed.replaceFirst("%timestamp%", Instant.now().toString());
            } else {
                thisEmbed = DOWN_EMBED_TEMPLATE.replaceFirst("%name%", monitor.getName());
                if (monitor.getType().equalsIgnoreCase("agent")) thisEmbed = thisEmbed.replaceFirst("%host%", "Server Agent");
                else thisEmbed = thisEmbed.replaceFirst("%host%", "["+monitor.getHost()+"]("+monitor.getHost()+")");
                thisEmbed = thisEmbed.replaceFirst("%timestamp%", Instant.now().toString());
            }
            Request req = new Request.Builder().url(discordWebhookURL).post(RequestBody.create(thisEmbed, MediaType.parse("application/json"))).build();
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
