package me.bartosz1.monitoring.models;

import me.bartosz1.monitoring.models.enums.NotificationType;

public class NotificationCDO {

    private String name;
    private NotificationType type;
    private String credentials;

    public String getName() {
        return name;
    }

    public NotificationCDO setName(String name) {
        this.name = name;
        return this;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationCDO setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public String getCredentials() {
        return credentials;
    }

    public NotificationCDO setCredentials(String credentials) {
        this.credentials = credentials;
        return this;
    }
}
