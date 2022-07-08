package me.bartosz1.monitoring.models.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.monitor.Monitor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationProvider {

    protected static final OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final Callback BLANK_HTTP_CALLBACK = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            if (response.body() != null) response.body().close();
            response.close();
        }
    };

    public void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
    }


}
