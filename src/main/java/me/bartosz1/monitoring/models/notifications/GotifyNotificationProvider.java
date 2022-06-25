package me.bartosz1.monitoring.models.notifications;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GotifyNotificationProvider implements NotificationProvider {

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
    private static final String REQUEST_BODY_TEMPLATE = "{ \"priority\": 7, \"title\": \"Monitoring alert\", \"message\": \"%name% is now %status%\" }";

    public static void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        String gotifyURL = contactList.getGotifyInstanceURL();
        if (gotifyURL.startsWith("http://") || gotifyURL.startsWith("https://")) {
            String gotifyURLValidated = gotifyURL;
            if (gotifyURL.endsWith("/")) gotifyURLValidated = gotifyURL.substring(0, gotifyURL.length() - 1);
            String body = REQUEST_BODY_TEMPLATE.replaceFirst("%name%", monitor.getName()).replaceFirst("%status%", monitor.getLastStatus().name());
            Request req = new Request.Builder().url(gotifyURLValidated + "/message").addHeader("X-Gotify-Key", contactList.getGotifyAppKey()).addHeader("Content-Type", "application/json").post(RequestBody.create(body, MediaType.parse("application/json"))).build();
            okHttpClient.newCall(req).enqueue(BLANK_HTTP_CALLBACK);
        }
    }

    //Blank because I have no clue what the response could be used for
    private static final Callback BLANK_HTTP_CALLBACK = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            if (response.body() != null) response.body().close();
            response.close();
        }
    };
}
