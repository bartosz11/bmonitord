package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Heartbeat;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

import java.io.IOException;
import java.time.Instant;

public class PingCheckProvider extends CheckProvider {

    public Heartbeat check(Monitor monitor) {
        try {
            long start = Instant.now().getEpochSecond();
            return ping(monitor.getHost(), monitor.getTimeout())
                    ? new Heartbeat().setMonitor(monitor).setTimestamp(Instant.now().getEpochSecond()).setStatus(MonitorStatus.UP).setResponseTime(Instant.now().getEpochSecond() - start)
                    : new Heartbeat().setMonitor(monitor).setTimestamp(Instant.now().getEpochSecond()).setStatus(MonitorStatus.DOWN);
        } catch (IOException | InterruptedException ignored) {
        }
        return new Heartbeat().setMonitor(monitor).setTimestamp(Instant.now().getEpochSecond()).setStatus(MonitorStatus.DOWN);
    }

    private boolean ping(String host, int timeout) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").contains("Windows"))
            //windows expects timeout in ms
            processBuilder.command("ping", "-n", "1", "-w", String.valueOf(timeout * 1000), host);
        else processBuilder.command("ping", "-c", "1", "-W", String.valueOf(timeout), host);
        int code = processBuilder.start().waitFor();
        return code == 0;
    }

}
