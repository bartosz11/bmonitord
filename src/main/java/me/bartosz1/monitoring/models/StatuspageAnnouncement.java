package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import me.bartosz1.monitoring.models.enums.StatuspageAnnouncementType;

@Entity
@Table(name = "statuspage_announcements")
public class StatuspageAnnouncement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JsonIncludeProperties({"id", "name"})
    private Statuspage statuspage;
    private String title;
    private String content;
    private StatuspageAnnouncementType type;

    public StatuspageAnnouncement() {
    }

    public StatuspageAnnouncement(StatuspageAnnouncementCDO cdo, Statuspage statuspage) {
        this.content = cdo.getContent();
        this.title = cdo.getTitle();
        this.type = cdo.getType();
        this.statuspage = statuspage;
    }

    public long getId() {
        return id;
    }

    public StatuspageAnnouncement setId(long id) {
        this.id = id;
        return this;
    }

    public Statuspage getStatuspage() {
        return statuspage;
    }

    public StatuspageAnnouncement setStatuspage(Statuspage statuspage) {
        this.statuspage = statuspage;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public StatuspageAnnouncement setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public StatuspageAnnouncement setContent(String content) {
        this.content = content;
        return this;
    }

    public StatuspageAnnouncementType getType() {
        return type;
    }

    public StatuspageAnnouncement setType(StatuspageAnnouncementType type) {
        this.type = type;
        return this;
    }
}
