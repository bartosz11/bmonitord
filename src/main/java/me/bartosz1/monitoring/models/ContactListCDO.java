package me.bartosz1.monitoring.models;

public class ContactListCDO {

    private String name;
    private String emailAddress;
    private String discordWebhookURL;
    private String slackWebhookURL;
    private String gotifyInstanceURL;
    private String gotifyAppKey;
    private String genericWebhookURL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSlackWebhookURL() {
        return slackWebhookURL;
    }

    public void setSlackWebhookURL(String slackWebhookURL) {
        this.slackWebhookURL = slackWebhookURL;
    }

    public String getGotifyInstanceURL() {
        return gotifyInstanceURL;
    }

    public void setGotifyInstanceURL(String gotifyInstanceURL) {
        this.gotifyInstanceURL = gotifyInstanceURL;
    }

    public String getGotifyAppKey() {
        return gotifyAppKey;
    }

    public void setGotifyAppKey(String gotifyAppKey) {
        this.gotifyAppKey = gotifyAppKey;
    }

    public String getGenericWebhookURL() {
        return genericWebhookURL;
    }

    public void setGenericWebhookURL(String genericWebhookURL) {
        this.genericWebhookURL = genericWebhookURL;
    }
}
