package me.bartosz1.monitoring;

import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.write.Point;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.monitor.Monitor;
import me.bartosz1.monitoring.models.monitor.MonitorStatus;
import me.bartosz1.monitoring.repos.IncidentRepository;
import me.bartosz1.monitoring.repos.MonitorRepository;
import me.bartosz1.monitoring.services.NotificationService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class CheckStatusTask implements InitializingBean {

    @Autowired
    private MonitorRepository monitorRepository;
    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private NotificationService notificationService;
    @Value("${monitoring.influxdb.enabled}")
    private boolean influxEnabled;
    //Default client settings
    private OkHttpClient httpClient = new OkHttpClient.Builder().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckStatusTask.class);
    private long initFinished;
    //I don't think there's a point in storing this in DB
    private final Map<Long, Integer> retryCount = new HashMap<>();
    private WriteApi influxWriteApi;

    @Scheduled(cron = "0 * * * * *")
    public void checkMonitors() {
        LOGGER.info("Checking monitors...");
        List<Monitor> monitors = monitorRepository.findAllMonitors();
        monitors.forEach(monitor -> {
            if (!monitor.isPaused()) {
                switch (monitor.getType()) {
                    case PING:
                        try {
                            long time = System.currentTimeMillis();
                            boolean reachable = ping(monitor.getHost(), monitor.getTimeout()*1000);
                            if (reachable) {
                                if (influxEnabled) influxWriteApi.writePoint(Point.measurement(String.valueOf(monitor.getId())).addField("responseTime", System.currentTimeMillis()-time));
                                retryCount.remove(monitor.getId());
                                processStatus(monitor, MonitorStatus.UP);
                            } else {
                                retryCount.merge(monitor.getId(), 1, Integer::sum);
                                //Works properly even when monitor.getRetries() == 0 because 1 > 0
                                if (retryCount.get(monitor.getId()) > monitor.getRetries())
                                    processStatus(monitor, MonitorStatus.DOWN);
                            }
                        } catch (IOException | InterruptedException e) {
                            //Maybe I should place the logic for "down" status here too /shrug
                            throw new RuntimeException(e);
                        }
                        break;
                    case HTTP:
                        OkHttpClient.Builder builder = httpClient.newBuilder();
                        //When user doesn't want his monitor's SSL to be verified, set all the "trust all certs" things
                        if (!monitor.getVerifySSL()) {
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
                            if (monitor.getAllowedHttpCodesAsList().contains(resp.code())) {
                                retryCount.remove(monitor.getId());
                                processStatus(monitor, MonitorStatus.UP);
                                if (influxEnabled) influxWriteApi.writePoint(Point.measurement(String.valueOf(monitor.getId())).addField("responseTime", resp.receivedResponseAtMillis()-resp.sentRequestAtMillis()));
                            }
                            //This is for invalid HTTP codes, even if the connection was successful
                            else {
                                retryCount.merge(monitor.getId(), 1, Integer::sum);
                                if (retryCount.get(monitor.getId()) > monitor.getRetries())
                                    processStatus(monitor, MonitorStatus.DOWN);
                                processStatus(monitor, MonitorStatus.DOWN);
                            }
                            //Just to prevent OkHttp printing some stuff to stdout
                            Objects.requireNonNull(resp.body()).close();
                            resp.close();
                        } catch (IOException e) {
                            //For failed requests
                            retryCount.merge(monitor.getId(), 1, Integer::sum);
                            if (retryCount.get(monitor.getId()) > monitor.getRetries())
                                processStatus(monitor, MonitorStatus.DOWN);
                            processStatus(monitor, MonitorStatus.DOWN);
                        }
                        break;
                    case AGENT:
                        //Agents shouldn't be checked if they have no way to send data
                        if (influxEnabled) {
                            //Check if 5 minutes since the start of application have passed, otherwise ignore agents while checking
                            if (initFinished + 300 < Instant.now().getEpochSecond()) {
                                if (monitor.getAgent().getLastDataReceived() + (long) monitor.getTimeout() < Instant.now().getEpochSecond()) {
                                    processStatus(monitor, MonitorStatus.DOWN);
                                } else {
                                    processStatus(monitor, MonitorStatus.UP);
                                }
                            }
                        }
                        break;
                }
                monitorRepository.save(monitor);
            }
        });
        LOGGER.info("Check finished.");
    }

    private final TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
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

    private final HostnameVerifier HOSTNAME_VERIFIER = (hostname, session) -> true;

    private void processStatus(Monitor monitor, MonitorStatus currentStatus) {
        incrementChecks(monitor, currentStatus);
        //Get incident sorted by start timestamp descending
        List<Incident> incidents = monitor.getIncidents().stream().sorted(Comparator.comparing(Incident::getStartTimestamp).reversed()).toList();
        if (monitor.getLastStatus() != MonitorStatus.UP && currentStatus == MonitorStatus.UP) {
            monitor.setLastStatus(currentStatus);
            if (incidents.isEmpty()) return;
            Incident lastIncident = incidents.get(0);
            if (lastIncident.isOngoing()) {
                lastIncident.setOngoing(false);
                lastIncident.setEndTimestamp(Instant.now().getEpochSecond());
                lastIncident.setDuration(lastIncident.getEndTimestamp() - lastIncident.getStartTimestamp());
                lastIncident = incidentRepository.save(lastIncident);
                notificationService.sendNotifications(monitor, lastIncident);
            }
        } else if (monitor.getLastStatus() != MonitorStatus.DOWN && currentStatus == MonitorStatus.DOWN) {
            monitor.setLastStatus(currentStatus);
            if (!incidents.isEmpty()) {
                Incident lastIncident = incidents.get(0);
                if (lastIncident.isOngoing()) return;
            }
            Incident incident = new Incident().setStartTimestamp(Instant.now().getEpochSecond()).setOngoing(true).setMonitor(monitor);
            incident = incidentRepository.save(incident);
            monitor.addIncident(incident);
            notificationService.sendNotifications(monitor, incident);
        }
    }

    //For the "5 minutes of tolerance" mechanism
    private void incrementChecks(Monitor monitor, MonitorStatus currentStatus) {
        if (currentStatus == MonitorStatus.UP) monitor.incrementChecksUp();
        else monitor.incrementChecksDown();
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        initFinished = Instant.now().getEpochSecond();
        LOGGER.info("Ignoring agent monitors in automated status checks for next 5 minutes.");
        if (influxEnabled) influxWriteApi = Monitoring.getInfluxClient().makeWriteApi(WriteOptions.builder().flushInterval(5000).batchSize(100).build());
    }

    private boolean ping(String host, int timeout) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").contains("Windows")) processBuilder.command("ping", "-n", "1", "-w", String.valueOf(timeout), host);
        else processBuilder.command("ping", "-c", "1", "-W", String.valueOf(timeout), host);
        int code = processBuilder.start().waitFor();
        return code==0;
    }
}
