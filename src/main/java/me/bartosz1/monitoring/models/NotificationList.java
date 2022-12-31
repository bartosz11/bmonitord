package me.bartosz1.monitoring.models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "notificationlists")
public class NotificationList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @OneToMany
    private List<Monitor> monitors;
    @ManyToOne
    private User user;
    private String discordWebhookURL;
    private String emailAddress;
    private String genericWebhookURL;
    private String gotifyAppKey;
    private String gotifyInstanceURL;
    private String pushbulletAPIToken;
    private String slackWebhookURL;

    public NotificationList(NotificationListCDO cdo, User user) {
        this.user = user;
        this.name = cdo.getName();
        this.emailAddress = cdo.getEmailAddress();
        this.pushbulletAPIToken = cdo.getPushbulletApiToken();
        this.discordWebhookURL = cdo.getDiscordWebhookURL();
        this.slackWebhookURL = cdo.getSlackWebhookURL();
        this.genericWebhookURL = cdo.getGenericWebhookURL();
        this.gotifyAppKey = cdo.getGotifyAppKey();
        this.gotifyInstanceURL = cdo.getGotifyInstanceURL();
    }

    public NotificationList() {}
    public long getId() {
        return id;
    }

    public NotificationList setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public NotificationList setName(String name) {
        this.name = name;
        return this;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public NotificationList setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public User getUser() {
        return user;
    }

    public NotificationList setUser(User user) {
        this.user = user;
        return this;
    }

    public String getDiscordWebhookURL() {
        return discordWebhookURL;
    }

    public NotificationList setDiscordWebhookURL(String discordWebhookURL) {
        this.discordWebhookURL = discordWebhookURL;
        return this;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public NotificationList setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public String getGenericWebhookURL() {
        return genericWebhookURL;
    }

    public NotificationList setGenericWebhookURL(String genericWebhookURL) {
        this.genericWebhookURL = genericWebhookURL;
        return this;
    }

    public String getGotifyAppKey() {
        return gotifyAppKey;
    }

    public NotificationList setGotifyAppKey(String gotifyAppKey) {
        this.gotifyAppKey = gotifyAppKey;
        return this;
    }

    public String getGotifyInstanceURL() {
        return gotifyInstanceURL;
    }

    public NotificationList setGotifyInstanceURL(String gotifyInstanceURL) {
        this.gotifyInstanceURL = gotifyInstanceURL;
        return this;
    }

    public String getPushbulletAPIToken() {
        return pushbulletAPIToken;
    }

    public NotificationList setPushbulletAPIToken(String pushbulletAPIToken) {
        this.pushbulletAPIToken = pushbulletAPIToken;
        return this;
    }

    public String getSlackWebhookURL() {
        return slackWebhookURL;
    }

    public NotificationList setSlackWebhookURL(String slackWebhookURL) {
        this.slackWebhookURL = slackWebhookURL;
        return this;
    }
}
