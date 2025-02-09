package one.bartosz.bmonitord.orchestrator.model.alarm;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("alarms_notifications")
//I don't think this "deserves" to be a GenericEntity even though it could be utilized in some ways, it's just a helper entity after all
public class AlarmNotification {

    @Id
    private UUID id;
    private UUID alarmId;
    private UUID notificationId;

    public UUID getId() {
        return id;
    }

    public AlarmNotification setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getAlarmId() {
        return alarmId;
    }

    public AlarmNotification setAlarmId(UUID alarmId) {
        this.alarmId = alarmId;
        return this;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public AlarmNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }
}
