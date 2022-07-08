package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.bartosz1.monitoring.models.monitor.Monitor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "contactlists")
public class ContactList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @JsonIgnore
    @ManyToOne
    private User user;
    @OneToMany
    @JsonIgnore
    private List<Monitor> monitors;
    private String name;
    private String emailAddress;
    private String discordWebhookURL;
    private String slackWebhookURL;
    private String gotifyInstanceURL;
    private String gotifyAppKey;
    private String genericWebhookURL;

    public ContactList() {
    }

    public ContactList(ContactListCDO cdo, User user) {
        this.user = user;
        this.discordWebhookURL = cdo.getDiscordWebhookURL();
        this.slackWebhookURL = cdo.getSlackWebhookURL();
        this.emailAddress = cdo.getEmailAddress();
        this.name = cdo.getName();
        this.gotifyAppKey = cdo.getGotifyAppKey();
        this.gotifyInstanceURL = cdo.getGotifyInstanceURL();
        this.genericWebhookURL = cdo.getGenericWebhookURL();
    }

    public long getId() {
        return id;
    }

    public ContactList setId(long id) {
        this.id = id;
        return this;
    }

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDiscordWebhookURL() {
        return discordWebhookURL;
    }

    public void setDiscordWebhookURL(String discordWebhookURL) {
        this.discordWebhookURL = discordWebhookURL;
    }

    public String getGenericWebhookURL() {
        return genericWebhookURL;
    }

    public void setGenericWebhookURL(String genericWebhookURL) {
        this.genericWebhookURL = genericWebhookURL;
    }

    public String getGotifyAppKey() {
        return gotifyAppKey;
    }

    public void setGotifyAppKey(String gotifyAppKey) {
        this.gotifyAppKey = gotifyAppKey;
    }

    public String getGotifyInstanceURL() {
        return gotifyInstanceURL;
    }

    public void setGotifyInstanceURL(String gotifyInstanceURL) {
        this.gotifyInstanceURL = gotifyInstanceURL;
    }

    public String getSlackWebhookURL() {
        return slackWebhookURL;
    }

    public void setSlackWebhookURL(String slackWebhookURL) {
        this.slackWebhookURL = slackWebhookURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
