package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorType;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PushbulletNotificationProvider extends NotificationProvider {

    private static final String NOTIFICATION_TEMPLATE = "{\"type\":\"note\",\"title\":\"Monitoring alert\",\"body\":\"%name% is now %status%. Host: %host%\"}";
    private static final String TEST_NOTIFICATION = "{\"type\":\"note\",\"title\":\"Monitoring\",\"body\":\"This is a test notification.\"}";

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String... args) {
        String token = args[0];
        String hostReplacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
        String body = NOTIFICATION_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%status%", monitor.getLastStatus().name()).replaceFirst("%host%", hostReplacement);
        Request req = new Request.Builder().url("https://api.pushbullet.com/v2/pushes").addHeader("Access-Token", token).addHeader("Content-Type", "application/json").post(RequestBody.create(body, MediaType.parse("application/json"))).build();
        super.getHttpClient().newCall(req).enqueue(BLANK_CALLBACK);
    }

    @Override
    public void sendTestNotification(String... args) {
        String token = args[0];
        Request req = new Request.Builder().url("https://api.pushbullet.com/v2/pushes").addHeader("Access-Token", token).addHeader("Content-Type", "application/json").post(RequestBody.create(TEST_NOTIFICATION, MediaType.parse("application/json"))).build();
        super.getHttpClient().newCall(req).enqueue(BLANK_CALLBACK);
    }
}
