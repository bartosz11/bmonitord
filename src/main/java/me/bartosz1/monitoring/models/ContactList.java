package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "contactlists")
public class ContactList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToMany
    @JsonIgnore
    private List<Monitor> monitors;
    private String name;
    private String emailAddress;
    private String discordWebhookURL;
    private String slackWebhookURL;
    private String gotifyInstanceURL;
    private String gotifyAppKey;
    private String genericWebhookURL;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
