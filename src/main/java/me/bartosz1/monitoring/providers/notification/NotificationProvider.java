package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;

public abstract class NotificationProvider {

    //Both methods are returning void because they're mostly asynchronous
    public abstract void sendNotification(Monitor monitor, Incident incident, String args);

    public abstract void sendTestNotification(String args);

}
