package me.bartosz1.monitoring.providers.notification;

import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.enums.MonitorStatus;
import me.bartosz1.monitoring.models.enums.MonitorType;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class EmailNotificationProvider extends NotificationProvider {

    private static final String UP_EMAIL_TEMPLATE = "Monitor: %name%\nHost: %host%\nTime: %timestamp%\nDuration: %duration%";
    private static final String DOWN_EMAIL_TEMPLATE = "Monitor: %name%\nHost: %host%\nTime: %timestamp%";
    private static final String TEST_NOTIFICATION = "This is a test notification.";
    private final JavaMailSender mailSender;
    private final DateTimeFormatter dateTimeFormatter;
    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    public EmailNotificationProvider(JavaMailSender mailSender, DateTimeFormatter dateTimeFormatter) {
        this.mailSender = mailSender;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public void sendNotification(Monitor monitor, Incident incident, String args) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(args);
        String replacement = monitor.getType() == MonitorType.AGENT ? "Server Agent" : monitor.getHost();
        String text;
        if (monitor.getLastStatus() == MonitorStatus.UP) {
            text = UP_EMAIL_TEMPLATE.replaceFirst("%name%", monitor.getName())
                    .replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration() * 1000, true, true))
                    .replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getEndTimestamp())))
                    .replaceFirst("%host%", replacement);
        } else {
            text = DOWN_EMAIL_TEMPLATE.replaceFirst("%name%", monitor.getName())
                    .replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getStartTimestamp())))
                    .replaceFirst("%host%", replacement);
        }
        //how to reduce code duplication 101
        message.setSubject(monitor.getLastStatus().name() + " - " + monitor.getName());
        message.setText(text);
        mailSender.send(message);
    }

    @Override
    public void sendTestNotification(String args) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(args);
        message.setSubject("Monitoring - test notification");
        message.setText(TEST_NOTIFICATION);
        mailSender.send(message);
    }
}
