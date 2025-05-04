package one.bartosz.bmonitord.checker.providers;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetHTTPInfo;
import one.bartosz.bmonitord.common.model.target.TargetStatus;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class HTTPCheckProvider extends CheckProvider {

    @Override
    public Heartbeat check(Target target) {
        TargetHTTPInfo httpInfo = target.getTargetHTTPInfo();
        Heartbeat baseHb = new Heartbeat().setTargetId(target.getId()).setTarget(target);
        if (httpInfo != null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder().callTimeout(target.getTimeout(), TimeUnit.SECONDS).followRedirects(httpInfo.isFollowRedirects());
            //SSL mess
            if (!httpInfo.isVerifySSLCertificate()) {
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, TRUST_MANAGERS, new java.security.SecureRandom());
                    builder.sslSocketFactory(sslContext.getSocketFactory(), TRUST_ALL_CERTS);
                    builder.hostnameVerifier(HOSTNAME_VERIFIER);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new RuntimeException(e);
                }
            }
            //Do the request
            OkHttpClient httpClient = builder.build();
            Request req = new Request.Builder().url(httpInfo.getHost()).build();
            try (Response resp = httpClient.newCall(req).execute()) {
                //We're not doing anything with the response body, so let's just close it
                if (resp.body() != null) resp.body().close();
                int code = resp.code();
                baseHb.setLatency(resp.receivedResponseAtMillis() - resp.sentRequestAtMillis()).setTimestamp(Instant.now());
                return httpInfo.getAllowedCodesAsList().contains(code) ? baseHb.setStatus(TargetStatus.UP) : baseHb.setStatus(TargetStatus.DOWN);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return baseHb.setTimestamp(Instant.now()).setStatus(TargetStatus.DOWN);
    }

    private static final X509TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    };
    private static final TrustManager[] TRUST_MANAGERS = new TrustManager[]{TRUST_ALL_CERTS};
    private static final HostnameVerifier HOSTNAME_VERIFIER = (hostname, session) -> true;
}
