package me.bartosz1.monitoring.providers.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class NotificationProvider {
    protected static final Callback BLANK_CALLBACK = new Callback() {
        //subject-to-change behaviors
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {

        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            Objects.requireNonNull(response.body()).close();
            response.close();
        }
    };
    //Utils
    private final OkHttpClient httpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    //I have to use autowired because I can't really make a constructor
    //todo change
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Europe/Warsaw"));

    //Args - things required to send the notification, specified as array because some providers might require more than one of these
    //Both methods are returning void because they're mostly asynchronous
    public abstract void sendNotification(Monitor monitor, Incident incident, String... args);

    public abstract void sendTestNotification(String... args);

    protected OkHttpClient getHttpClient() {
        return httpClient;
    }

    protected DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }


}
