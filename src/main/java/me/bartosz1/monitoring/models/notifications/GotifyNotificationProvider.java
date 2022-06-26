package me.bartosz1.monitoring.models.notifications;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

@Component
public class GotifyNotificationProvider extends NotificationProvider {

    private static final String REQUEST_BODY_TEMPLATE = "{ \"priority\": 7, \"title\": \"Monitoring alert\", \"message\": \"%name% is now %status%\" }";

    public void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String gotifyURL = contactList.getGotifyInstanceURL();
        if (gotifyURL.startsWith("http://") || gotifyURL.startsWith("https://")) {
            String gotifyURLValidated = gotifyURL;
            if (gotifyURL.endsWith("/")) gotifyURLValidated = gotifyURL.substring(0, gotifyURL.length() - 1);
            String body = REQUEST_BODY_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%status%", monitor.getLastStatus().name());
            Request req = new Request.Builder().url(gotifyURLValidated + "/message").addHeader("X-Gotify-Key", contactList.getGotifyAppKey()).addHeader("Content-Type", "application/json").post(RequestBody.create(body, MediaType.parse("application/json"))).build();
            NotificationProvider.okHttpClient.newCall(req).enqueue(NotificationProvider.BLANK_HTTP_CALLBACK);
        }
    }
}
