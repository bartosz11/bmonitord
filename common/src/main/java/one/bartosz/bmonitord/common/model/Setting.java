package one.bartosz.bmonitord.common.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("settings")
public class Setting {

    @Id
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public Setting setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Setting setValue(String value) {
        this.value = value;
        return this;
    }
}
