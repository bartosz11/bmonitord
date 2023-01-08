package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;
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
    private static final HostnameVerifier HOSTNAME_VERIFIER = (hostname, session) -> true;
    private OkHttpClient httpClient = new OkHttpClient.Builder().build();

    public Heartbeat check(Monitor monitor) {
        OkHttpClient.Builder builder = httpClient.newBuilder();
        //When user doesn't want his monitor's SSL to be verified, set all the "trust all certs" things
        if (!monitor.isVerifyCertificate()) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{TRUST_ALL_CERTS}, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) TRUST_ALL_CERTS);
                builder.hostnameVerifier(HOSTNAME_VERIFIER);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
        //Rebuild the client with monitor's timeout
        httpClient = builder.callTimeout(monitor.getTimeout(), TimeUnit.SECONDS).build();
        try {
            //Send request, if response code fits in user defined range, consider as up
            Response resp = httpClient.newCall(new Request.Builder().url(monitor.getHost()).build()).execute();
            //Just to prevent OkHttp printing some stuff to std out
            Objects.requireNonNull(resp.body()).close();
            int code = resp.code();
            resp.close();
            if (monitor.getAllowedHttpCodesAsList().contains(code)) {
                return new Heartbeat().setMonitor(monitor).setStatus(MonitorStatus.UP).setResponseTime(resp.receivedResponseAtMillis()-resp.sentRequestAtMillis()).setTimestamp(Instant.now().getEpochSecond());
                //This else is for invalid HTTP codes, even if the connection was successful
            } else return new Heartbeat().setMonitor(monitor).setStatus(MonitorStatus.DOWN).setResponseTime(resp.receivedResponseAtMillis()-resp.sentRequestAtMillis()).setTimestamp(Instant.now().getEpochSecond());
        } catch (IOException e) {
            //For failed requests, no response time here because server probably hasn't responded at all
            return new Heartbeat().setMonitor(monitor).setStatus(MonitorStatus.DOWN).setTimestamp(Instant.now().getEpochSecond());
        }

    }
}
