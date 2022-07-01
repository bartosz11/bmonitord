package me.bartosz1.monitoring.services;

import me.bartosz1.monitoring.models.ContactList;
import me.bartosz1.monitoring.models.Incident;
import me.bartosz1.monitoring.models.Monitor;
import me.bartosz1.monitoring.models.MonitorStatus;
import me.bartosz1.monitoring.models.notifications.NotificationProvider;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class EmailService extends NotificationProvider {
    //Ok I know I should've used thymeleaf but is there a point for this simple messages?
    private static final String UP_EMAIL_TEMPLATE = "Monitor: %name%\nHost: %host%\nTime: %timestamp%\nDuration: %duration%";
    private static final String DOWN_EMAIL_TEMPLATE = "Monitor: %name%\nHost: %host%\nTime: %timestamp%";
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private DateTimeFormatter dateTimeFormatter;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    public void sendNotification(Monitor monitor, ContactList contactList, Incident incident) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(contactList.getEmailAddress());
        String messageText;
        if (monitor.getLastStatus() == MonitorStatus.UP) {
            message.setSubject("UP - " + monitor.getName());
            messageText = UP_EMAIL_TEMPLATE.replaceFirst("%name%", monitor.getName());
            if (monitor.getType().equalsIgnoreCase("agent"))
                messageText = messageText.replaceFirst("%host%", "Server Agent");
            else messageText = messageText.replaceFirst("%host%", monitor.getHost());
            messageText = messageText.replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getEndTimestamp())));
            messageText = messageText.replaceFirst("%duration%", DurationFormatUtils.formatDurationWords(incident.getDuration() * 1000, true, true));
        } else {
            message.setSubject("DOWN - " + monitor.getName());
            messageText = DOWN_EMAIL_TEMPLATE.replaceFirst("%name%", monitor.getName());
            if (monitor.getType().equalsIgnoreCase("agent"))
                messageText = messageText.replaceFirst("%host%", "Server Agent");
            else messageText = messageText.replaceFirst("%host%", monitor.getHost());
            messageText = messageText.replaceFirst("%timestamp%", dateTimeFormatter.format(Instant.ofEpochSecond(incident.getStartTimestamp())));
        }
        message.setText(messageText);
        mailSender.send(message);
    }
}
