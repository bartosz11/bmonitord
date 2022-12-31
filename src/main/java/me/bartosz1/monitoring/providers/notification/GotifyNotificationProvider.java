package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorType;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GotifyNotificationProvider extends NotificationProvider {
    //Yes, Gotify notifications contain less info than others and it'll stay like this I think, push notifications aren't really supposed to be bulky
    private static final String REQUEST_BODY_TEMPLATE = "{ \"priority\": 7, \"title\": \"Monitoring alert\", \"message\": \"%name% is now %status%. Host: %host%\" }";
    private static final String TEST_NOTIFICATION = "{ \"priority\": 4, \"title\": \"Monitoring\", \"message\": \"This is a test notification.\" }";

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String... args) {
        String gotifyURL = args[0].endsWith("/") ? args[0].substring(0, args[0].length() - 1) : args[0];
        String gotifyToken = args[1];
        String hostReplacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
        String body = REQUEST_BODY_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%status%", monitor.getLastStatus().name()).replaceFirst("%host%", hostReplacement);
        Request req = new Request.Builder().url(gotifyURL + "/message").addHeader("X-Gotify-Key", gotifyToken).addHeader("Content-Type", "application/json").post(RequestBody.create(body, MediaType.parse("application/json"))).build();
        super.getHttpClient().newCall(req).enqueue(BLANK_CALLBACK);
    }

    @Override
    public void sendTestNotification(String... args) {
        String gotifyURL = args[0].endsWith("/") ? args[0].substring(0, args[0].length() - 1) : args[0];
        String gotifyToken = args[1];
        Request req = new Request.Builder().url(gotifyURL + "/message")
                .addHeader("X-Gotify-Key", gotifyToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(TEST_NOTIFICATION, MediaType.parse("application/json"))).build();
        super.getHttpClient().newCall(req).enqueue(BLANK_CALLBACK);
    }
}
