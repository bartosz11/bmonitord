package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class HTTPCheckProvider extends CheckProvider {

    private static final TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    };
    @Override
    public Heartbeat check(Monitor monitor) {
        try {
            HttpClient.Builder builder = HttpClient.newBuilder();
            if (!monitor.getHttpInfo().isVerifyCertificate()) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{TRUST_ALL_CERTS}, new SecureRandom());
                builder.sslContext(sslContext);
            }
            builder.followRedirects(HttpClient.Redirect.ALWAYS);
            HttpClient httpClient = builder.build();
            HttpRequest req = HttpRequest.newBuilder().GET().uri(new URI(monitor.getHost())).timeout(Duration.of(monitor.getTimeout(), TimeUnit.SECONDS.toChronoUnit())).build();
            long start = Instant.now().toEpochMilli();
            HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            long latency = Instant.now().toEpochMilli() - start;
            Heartbeat heartbeat = new Heartbeat().setMonitor(monitor).setResponseTime(latency).setTimestamp(Instant.now().getEpochSecond());
            //ternary expressions are cool
            return monitor.getHttpInfo().getAllowedHttpCodesAsList().contains(resp.statusCode())
                    ? heartbeat.setStatus(MonitorStatus.UP)
                    : heartbeat.setStatus(MonitorStatus.DOWN);
            //we're ignoring all exceptions that can be thrown - request will fail no matter which one is thrown,
            //so we can just return "DOWN"
        } catch (IOException | InterruptedException | URISyntaxException | NoSuchAlgorithmException | KeyManagementException ignored) {}
        return new Heartbeat().setMonitor(monitor).setStatus(MonitorStatus.DOWN).setTimestamp(Instant.now().getEpochSecond());
    }

}
