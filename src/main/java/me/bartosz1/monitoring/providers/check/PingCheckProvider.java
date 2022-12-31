package me.bartosz1.monitoring.providers.check;

import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;

import java.io.IOException;

public class PingCheckProvider extends CheckProvider {

    public MonitorStatus check(Monitor monitor) {
        try {
            return ping(monitor.getHost(), monitor.getTimeout() * 1000) ? MonitorStatus.UP : MonitorStatus.DOWN;
        } catch (IOException | InterruptedException ignored) {
        }
        return MonitorStatus.DOWN;
    }

    private boolean ping(String host, int timeout) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").contains("Windows"))
            processBuilder.command("ping", "-n", "1", "-w", String.valueOf(timeout), host);
        else processBuilder.command("ping", "-c", "1", "-W", String.valueOf(timeout), host);
        int code = processBuilder.start().waitFor();
        return code == 0;
    }

}
