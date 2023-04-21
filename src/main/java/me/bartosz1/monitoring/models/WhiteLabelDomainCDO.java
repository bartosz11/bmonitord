package me.bartosz1.monitoring.models;

public class WhiteLabelDomainCDO {

    private String name;
    private String domain;

    public String getName() {
        return name;
    }

    public WhiteLabelDomainCDO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public WhiteLabelDomainCDO setDomain(String domain) {
        this.domain = domain;
        return this;
    }
}
