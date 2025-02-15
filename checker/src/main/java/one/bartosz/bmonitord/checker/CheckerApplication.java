package one.bartosz.bmonitord.checker;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CheckerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CheckerApplication.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public OkHttpClient okHttpClient(@Value("${bmonitord.checker.timeout}") int timeout) {
        return new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.SECONDS).build();
    }

}
