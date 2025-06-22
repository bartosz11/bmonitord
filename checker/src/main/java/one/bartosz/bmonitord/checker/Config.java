package one.bartosz.bmonitord.checker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "bmonitord.checker")
public class Config {

    private String key;
    private List<String> orchestrators;

    public String getKey() {
        return key;
    }

    public Config setKey(String key) {
        this.key = key;
        return this;
    }

    public List<String> getOrchestrators() {
        return orchestrators;
    }

    public Config setOrchestrators(List<String> orchestrators) {
        this.orchestrators = orchestrators;
        return this;
    }
}
