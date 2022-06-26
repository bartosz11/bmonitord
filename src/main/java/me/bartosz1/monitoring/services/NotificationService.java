package me.bartosz1.monitoring.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.notifications.DiscordNotificationProvider;
import me.bartosz1.monitoring.models.notifications.GenericWebhookNotificationProvider;
import me.bartosz1.monitoring.models.notifications.GotifyNotificationProvider;
import me.bartosz1.monitoring.models.notifications.SlackNotificationProvider;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NotificationService {
    @Autowired
    private EmailService emailService;
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //I know this might not be the best approach, I'm just "prototyping" it or something - this entire software is a huge work in progress and far from perfection
    public void sendNotifications(Monitor monitor, Incident incident) {
        ContactList contactList = monitor.getContactList();
        if (contactList.getGotifyAppKey() != null && contactList.getGotifyInstanceURL() != null) {
            GotifyNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getDiscordWebhookURL() != null) {
            DiscordNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getSlackWebhookURL() != null) {
            SlackNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getGenericWebhookURL() != null) {
            GenericWebhookNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getEmailAddress() != null) {
            emailService.sendNotification(monitor, contactList, incident);
        }
    }

}
