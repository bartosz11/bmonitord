package one.bartosz.bmonitord.common.model;

//This is not persisted in the DB, obv
public class WebSocketMessageDTO {

    private String type;
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
