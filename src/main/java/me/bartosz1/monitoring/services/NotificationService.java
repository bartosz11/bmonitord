package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.notifications.DiscordNotificationProvider;
import me.bartosz1.monitoring.models.notifications.GenericWebhookNotificationProvider;
import me.bartosz1.monitoring.models.notifications.GotifyNotificationProvider;
import me.bartosz1.monitoring.models.notifications.SlackNotificationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {
    @Autowired
    private EmailService emailService;
    @Autowired
    private DiscordNotificationProvider discordNotificationProvider;
    @Autowired
    private SlackNotificationProvider slackNotificationProvider;
    @Autowired
    private GotifyNotificationProvider gotifyNotificationProvider;
    @Autowired
    private GenericWebhookNotificationProvider genericWebhookNotificationProvider;

    //I know this might not be the best approach, I'm just "prototyping" it or something - this entire software is a huge work in progress and far from perfection
    public void sendNotifications(Monitor monitor, Incident incident) {
        ContactList contactList = monitor.getContactList();
        if (contactList == null) return;
        if (contactList.getGotifyAppKey() != null && contactList.getGotifyInstanceURL() != null) {
            gotifyNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getDiscordWebhookURL() != null) {
            discordNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getSlackWebhookURL() != null) {
            slackNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getGenericWebhookURL() != null) {
            genericWebhookNotificationProvider.sendNotification(monitor, contactList, incident);
        }
        if (contactList.getEmailAddress() != null) {
            emailService.sendNotification(monitor, contactList, incident);
        }
    }

}
