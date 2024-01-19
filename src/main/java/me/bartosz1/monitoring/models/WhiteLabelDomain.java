package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "white_label_domains")
public class WhiteLabelDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String domain;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    //for now
    @JsonIgnore
    private Statuspage statuspage;

    public WhiteLabelDomain() {
    }

    public WhiteLabelDomain(WhiteLabelDomainCDO cdo, User user) {
        this.user = user;
        this.name = cdo.getName();
        this.domain = cdo.getDomain();
    }

    public long getId() {
        return id;
    }

    public WhiteLabelDomain setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public WhiteLabelDomain setName(String name) {
        this.name = name;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public WhiteLabelDomain setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public User getUser() {
        return user;
    }

    public WhiteLabelDomain setUser(User user) {
        this.user = user;
        return this;
    }

    public Statuspage getStatuspage() {
        return statuspage;
    }

    public WhiteLabelDomain setStatuspage(Statuspage statuspage) {
        this.statuspage = statuspage;
        return this;
    }
}
