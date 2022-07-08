package me.bartosz1.monitoring;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EntityScan("me.bartosz1.monitoring.models")
@ComponentScan("me.bartosz1.monitoring")
@EnableScheduling
public class Monitoring implements InitializingBean {

    private static InfluxDBClient influxClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(Monitoring.class);
    @Value("${monitoring.influxdb.url}")
    private String influxURL;
    @Value("${monitoring.influxdb.token}")
    private String influxToken;
    @Value("${monitoring.influxdb.bucket}")
    private String influxBucket;

    @Value("${monitoring.influxdb.organization}")
    private String influxOrganization;
    @Value("${monitoring.timezone}")
    private String timezone;
    @Value("${monitoring.influxdb.enabled}")
    private boolean influxEnabled;

    //Fixes circular dependency issue
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of(timezone));
    }
    @Bean
    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }

    public static void main(String[] args) {
        SpringApplication.run(Monitoring.class);
    }

    public static InfluxDBClient getInfluxClient() {
        return influxClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (influxEnabled) influxClient = InfluxDBClientFactory.create(influxURL, influxToken.toCharArray(), influxOrganization, influxBucket);
        else LOGGER.warn("InfluxDB integration is DISABLED. Monitors with type \"agent\" won't be able to send data and won't be checked.");
    }
}
