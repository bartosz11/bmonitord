package me.bartosz1.monitoring.models.notifications;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;

public interface NotificationProvider {
    //I don't think there's a point in making this non-static
    //OkHttpClients don't need to change every single time and they actually don't, timeout or anything isn't even configurable
    static void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {

    }
}
