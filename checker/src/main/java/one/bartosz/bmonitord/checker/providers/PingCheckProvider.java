package one.bartosz.bmonitord.checker.providers;

import one.bartosz.bmonitord.common.model.Heartbeat;
import one.bartosz.bmonitord.common.model.target.Target;
import one.bartosz.bmonitord.common.model.target.TargetPingInfo;
import one.bartosz.bmonitord.common.model.target.TargetStatus;

import java.io.IOException;
import java.time.Instant;

public class PingCheckProvider extends CheckProvider {

    @Override
    public Heartbeat check(Target target) {
        Heartbeat baseHb = new Heartbeat().setTarget(target).setTargetId(target.getId());
        TargetPingInfo targetPingInfo = target.getTargetPingInfo();
        //just to be safe
        if (targetPingInfo != null) {
            try {
                long start = Instant.now().toEpochMilli();
                return ping(targetPingInfo.getHost(), targetPingInfo.getTimeout())
                        ? baseHb.setTimestamp(Instant.now()).setStatus(TargetStatus.UP).setLatency(Instant.now().toEpochMilli() - start)
                        : baseHb.setTimestamp(Instant.now()).setStatus(TargetStatus.DOWN);
            } catch (IOException | InterruptedException ignored) {}
        }
        return baseHb.setTimestamp(Instant.now()).setStatus(TargetStatus.DOWN);
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
