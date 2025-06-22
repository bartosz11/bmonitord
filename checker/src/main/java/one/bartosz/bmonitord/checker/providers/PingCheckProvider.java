package one.bartosz.bmonitord.checker.providers;


import one.bartosz.bmonitord.checker.models.Heartbeat;
import one.bartosz.bmonitord.checker.models.Target;
import one.bartosz.bmonitord.checker.models.TargetPingInfo;

import java.io.IOException;
import java.time.Instant;

public class PingCheckProvider extends CheckProvider {

    @Override
    public Heartbeat check(Target target) {
        Heartbeat baseHb = new Heartbeat().setTargetID(target.getID());
        TargetPingInfo targetPingInfo = target.getPingInfo();
        //just to be safe
        if (targetPingInfo != null) {
            try {
                long start = Instant.now().toEpochMilli();
                return ping(targetPingInfo.getHost(), target.getTimeout())
                        ? baseHb.setTimestamp(Instant.now()).setStatus(0).setLatency(Instant.now().toEpochMilli() - start)
                        : baseHb.setTimestamp(Instant.now()).setStatus(1);
            } catch (IOException | InterruptedException ignored) {
            }
        }
        return baseHb.setTimestamp(Instant.now()).setStatus(1);
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
