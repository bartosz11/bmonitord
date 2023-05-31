package me.bartosz1.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
@EnableAutoConfiguration
public class Monitoring {

    @Value("${monitoring.timezone}")
    private String timezone;

    public static void main(String[] args) {
        SpringApplication.run(Monitoring.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }

    //beans used by notification providers
    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of(timezone));
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.of(5, ChronoUnit.SECONDS)).build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
