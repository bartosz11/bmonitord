package one.bartosz.bmonitord.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication(scanBasePackages = {"one.bartosz.bmonitord.common", "one.bartosz.bmonitord.orchestrator"})
@EnableR2dbcRepositories
@EnableScheduling
public class OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }

    @Bean
    public ConcurrentHashMap<UUID, StatusProcessingTask> heartbeatQueues() {
        return new ConcurrentHashMap<>();
    }

}
