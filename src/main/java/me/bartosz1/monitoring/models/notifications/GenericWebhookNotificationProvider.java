package me.bartosz1.monitoring.models.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

import java.util.HashMap;
@Component
public class GenericWebhookNotificationProvider extends NotificationProvider{

    public void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String url = contactList.getGenericWebhookURL();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            HashMap<String, Object> body = new HashMap<>();
            body.put("status", monitor.getLastStatus().name());
            //Maybe we shouldn't send entire monitor and incident object?
            body.put("monitor", monitor);
            body.put("incident", incident);
            try {
                String finalBody = NotificationProvider.objectMapper.writeValueAsString(body);
                Request req = new Request.Builder().url(url).post(RequestBody.create(finalBody, MediaType.parse("application/json"))).build();
                NotificationProvider.okHttpClient.newCall(req).enqueue(NotificationProvider.BLANK_HTTP_CALLBACK);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
