package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "access_tokens")
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    @JsonIgnore
    private User user;
    @JsonIgnore
    private String token;
    private String shortToken;
    private String name;

    public long getId() {
        return id;
    }

    public AccessToken setUser(User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public AccessToken setToken(String token) {
        this.token = token;
        this.shortToken = token.substring(0, 16);
        return this;
    }

    public String getName() {
        return name;
    }

    public AccessToken setName(String name) {
        this.name = name;
        return this;
    }

    public String getShortToken() {
        return shortToken;
    }

}
