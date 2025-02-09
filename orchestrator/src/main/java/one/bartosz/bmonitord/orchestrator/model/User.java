package one.bartosz.bmonitord.orchestrator.model;

import one.bartosz.bmonitord.orchestrator.model.target.Target;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("users")
public class User extends GenericEntity<User> {

    private String username;
    private String password;
    private boolean enabled;
    @Transient
    private List<Target> targets;
    @Transient
    private List<Notification> notifications;


    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public User setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public User setTargets(List<Target> targets) {
        this.targets = targets;
        return this;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public User setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }
}
