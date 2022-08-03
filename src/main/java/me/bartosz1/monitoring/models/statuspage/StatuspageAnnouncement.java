package me.bartosz1.monitoring.models.statuspage;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class StatuspageAnnouncement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JsonIgnore
    private Statuspage statuspage;
    private StatuspageAnnouncementType type;
    private String title;
    private String content;

    public StatuspageAnnouncement() {}
    public StatuspageAnnouncement(StatuspageAnnouncementCDO cdo, Statuspage statuspage) {
        this.statuspage = statuspage;
        this.type = cdo.getType();
        this.content = cdo.getContent();
        this.title = cdo.getTitle();
    }

    public Statuspage getStatuspage() {
        return statuspage;
    }

    public StatuspageAnnouncement setStatuspage(Statuspage statuspage) {
        this.statuspage = statuspage;
        return this;
    }

    public StatuspageAnnouncementType getType() {
        return type;
    }

    public StatuspageAnnouncement setType(StatuspageAnnouncementType type) {
        this.type = type;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public StatuspageAnnouncement setContent(String content) {
        this.content = content;
        return this;
    }

    public long getId() {
        return id;
    }

    public StatuspageAnnouncement setId(long id) {
        this.id = id;
        return this;
    }
}
