package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public abstract class NotificationProvider {
    protected static final Callback BLANK_CALLBACK = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {

        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            Objects.requireNonNull(response.body()).close();
            response.close();
        }
    };

    //Both methods are returning void because they're mostly asynchronous
    public abstract void sendNotification(Monitor monitor, Incident incident, String args);

    public abstract void sendTestNotification(String args);

}
