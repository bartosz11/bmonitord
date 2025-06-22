package one.bartosz.bmonitord.checker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrchestratorConnectionManager {

    private final OkHttpClient okHttpClient;
    private final List<String> orchestrators;
    private final OrchestratorConnectionHandler connectionHandler;
    private String nextOrchestrator;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorConnectionManager.class);

    public OrchestratorConnectionManager(OkHttpClient okHttpClient, @Lazy OrchestratorConnectionHandler connectionHandler, Config config) {
        this.okHttpClient = okHttpClient;
        this.connectionHandler = connectionHandler;
        this.orchestrators = config.getOrchestrators().stream().map(url -> {
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
