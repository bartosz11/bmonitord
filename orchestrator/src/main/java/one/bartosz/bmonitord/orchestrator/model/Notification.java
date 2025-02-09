package one.bartosz.bmonitord.orchestrator.model;

import one.bartosz.bmonitord.orchestrator.model.alarm.Alarm;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("notifications")
public class Notification extends GenericEntity<Notification> {

    private String name;
    private NotificationType type;
    private String credentials;
    private UUID userId;
    @Transient
    private User user;
    @Transient
    private List<Alarm> alarms;

    public String getName() {
        return name;
    }

    public Notification setName(String name) {
        this.name = name;
        return this;
    }

    public NotificationType getType() {
        return type;
    }

    public Notification setType(NotificationType type) {
        this.type = type;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public Notification setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }

    public Notification setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Notification setUser(User user) {
        this.user = user;
        return this;
    }

    public String getCredentials() {
        return credentials;
    }

    public Notification setCredentials(String credentials) {
        this.credentials = credentials;
        return this;
    }
}
