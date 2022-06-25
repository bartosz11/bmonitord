package me.bartosz1.monitoring.models.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class GenericWebhookNotificationProvider implements NotificationProvider{

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String url = contactList.getGenericWebhookURL();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            HashMap<String, Object> body = new HashMap<>();
            body.put("status", monitor.getLastStatus().name());
            //Maybe we shouldn't send entire monitor and incident object?
            body.put("monitor", monitor);
            body.put("incident", incident);
            try {
                String finalBody = objectMapper.writeValueAsString(body);
                Request req = new Request.Builder().url(url).post(RequestBody.create(finalBody, MediaType.parse("application/json"))).build();
                okHttpClient.newCall(req).enqueue(BLANK_HTTP_CALLBACK);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static final Callback BLANK_HTTP_CALLBACK = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            response.body().close();
            response.close();
        }
    };
}
