package one.bartosz.bmonitord.checker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrchestratorConnectionManager {

    private final OkHttpClient okHttpClient;
    private final List<String> orchestrators;
    private final OrchestratorConnectionHandler connectionHandler;
    private String nextOrchestrator;

    public OrchestratorConnectionManager(OkHttpClient okHttpClient, @Lazy OrchestratorConnectionHandler connectionHandler, @Value("${bmonitord.checker.orchestrators}") String[] orchestrators) {
        this.okHttpClient = okHttpClient;
        this.connectionHandler = connectionHandler;
        this.orchestrators = Arrays.stream(orchestrators).map(url -> {
            if (url.endsWith("/")) return url.substring(0, url.length() - 1);
            else return url;
        }).toList();
        this.nextOrchestrator = this.orchestrators.getFirst();
        connect();
    }

    //I'm not sure if this should be called "connect"
    public void connect() {
        connect(nextOrchestrator);
        int nextIndex = orchestrators.indexOf(nextOrchestrator) + 1;
        if (nextIndex == orchestrators.size()) {
            nextIndex = 0;
        }
        nextOrchestrator = orchestrators.get(nextIndex);
    }

    public void connect(String url) {
        Request req = new Request.Builder().url(url + "/orchestrator/ws").build();
        okHttpClient.newWebSocket(req, connectionHandler);
    }

    public void setNextOrchestrator(String nextOrchestrator) {
        this.nextOrchestrator = nextOrchestrator;
    }
}
