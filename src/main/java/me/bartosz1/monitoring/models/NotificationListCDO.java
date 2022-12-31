package me.bartosz1.monitoring.models;

public class NotificationListCDO {

    private String name;
    private String discordWebhookURL;
    private String emailAddress;
    private String genericWebhookURL;
    private String gotifyAppKey;
    private String gotifyInstanceURL;
    private String pushbulletApiToken;
    private String slackWebhookURL;

    public String getName() {
        return name;
    }

    public NotificationListCDO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDiscordWebhookURL() {
        return discordWebhookURL;
    }

    public NotificationListCDO setDiscordWebhookURL(String discordWebhookURL) {
        this.discordWebhookURL = discordWebhookURL;
        return this;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public NotificationListCDO setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public String getGenericWebhookURL() {
        return genericWebhookURL;
    }

    public NotificationListCDO setGenericWebhookURL(String genericWebhookURL) {
        this.genericWebhookURL = genericWebhookURL;
        return this;
    }

    public String getGotifyAppKey() {
        return gotifyAppKey;
    }

    public NotificationListCDO setGotifyAppKey(String gotifyAppKey) {
        this.gotifyAppKey = gotifyAppKey;
        return this;
    }

    public String getGotifyInstanceURL() {
        return gotifyInstanceURL;
    }

    public NotificationListCDO setGotifyInstanceURL(String gotifyInstanceURL) {
        this.gotifyInstanceURL = gotifyInstanceURL;
        return this;
    }

    public String getPushbulletApiToken() {
        return pushbulletApiToken;
    }

    public NotificationListCDO setPushbulletApiToken(String pushbulletApiToken) {
        this.pushbulletApiToken = pushbulletApiToken;
        return this;
    }

    public String getSlackWebhookURL() {
        return slackWebhookURL;
    }

    public NotificationListCDO setSlackWebhookURL(String slackWebhookURL) {
        this.slackWebhookURL = slackWebhookURL;
        return this;
    }
}
