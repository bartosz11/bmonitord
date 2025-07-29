package one.bartosz.bmonitord.checker.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketMessageDTO {

    @JsonProperty("type")
    private String type;
    @JsonProperty("payload")
    private Object payload;

    public String getType() {
        return type;
    }

    public WebSocketMessageDTO setType(String type) {
        this.type = type;
        return this;
    }


    public Object getPayload() {
        return payload;
    }

    public WebSocketMessageDTO setPayload(Object payload) {
        this.payload = payload;
        return this;
    }
}
