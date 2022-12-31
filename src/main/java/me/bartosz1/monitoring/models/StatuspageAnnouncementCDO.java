package me.bartosz1.monitoring.models;

import me.bartosz1.monitoring.models.enums.StatuspageAnnouncementType;

public class StatuspageAnnouncementCDO {

    private String title;
    private String content;
    private StatuspageAnnouncementType type;

    public String getTitle() {
        return title;
    }

    public StatuspageAnnouncementCDO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public StatuspageAnnouncementCDO setContent(String content) {
        this.content = content;
        return this;
    }

    public StatuspageAnnouncementType getType() {
        return type;
    }

    public StatuspageAnnouncementCDO setType(StatuspageAnnouncementType type) {
        this.type = type;
        return this;
    }
}
