package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.Notification;
import me.bartosz1.monitoring.models.NotificationType;
import me.bartosz1.monitoring.providers.notification.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
//this class shows how to avoid boilerplate using boilerplate
public class NotificationSenderService implements InitializingBean {

    private HashMap<NotificationType, NotificationProvider> lookup;
    private final DiscordNotificationProvider discordNotificationProvider;
    private final EmailNotificationProvider emailNotificationProvider;
    private final GenericWebhookNotificationProvider genericWebhookNotificationProvider;
    private final GotifyNotificationProvider gotifyNotificationProvider;
    private final PushbulletNotificationProvider pushbulletNotificationProvider;
    private final SlackNotificationProvider slackNotificationProvider;

    public NotificationSenderService(DiscordNotificationProvider discordNotificationProvider, EmailNotificationProvider emailNotificationProvider, GenericWebhookNotificationProvider genericWebhookNotificationProvider, GotifyNotificationProvider gotifyNotificationProvider, PushbulletNotificationProvider pushbulletNotificationProvider, SlackNotificationProvider slackNotificationProvider) {
        this.discordNotificationProvider = discordNotificationProvider;
        this.emailNotificationProvider = emailNotificationProvider;
        this.genericWebhookNotificationProvider = genericWebhookNotificationProvider;
        this.gotifyNotificationProvider = gotifyNotificationProvider;
        this.pushbulletNotificationProvider = pushbulletNotificationProvider;
        this.slackNotificationProvider = slackNotificationProvider;
    }

    public void sendNotification(Notification notification, Monitor monitor, Incident incident) {
        lookup.get(notification.getType()).sendNotification(monitor, incident, notification.getCredentials());
    }

    public void sendTestNotification(Notification notification) {
        lookup.get(notification.getType()).sendTestNotification(notification.getCredentials());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        HashMap<NotificationType, NotificationProvider> lookup = new HashMap<>();
        lookup.put(NotificationType.DISCORD, discordNotificationProvider);
        lookup.put(NotificationType.EMAIL, emailNotificationProvider);
        lookup.put(NotificationType.GENERIC_WEBHOOK, genericWebhookNotificationProvider);
        lookup.put(NotificationType.GOTIFY, gotifyNotificationProvider);
        lookup.put(NotificationType.PUSHBULLET, pushbulletNotificationProvider);
        lookup.put(NotificationType.SLACK, slackNotificationProvider);
        this.lookup = lookup;
    }
}
