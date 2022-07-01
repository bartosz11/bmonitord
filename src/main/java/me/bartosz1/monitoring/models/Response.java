package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private final String status;
    private final HashMap<String, Object> additionalInfo = new HashMap<>();
    private Object data;

    public Response(String status) {
        this.status = status;
    }

    //Jackson's using these getters I guess
    @JsonAnyGetter
    public HashMap<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public String getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public Response addAdditionalInfo(String key, Object value) {
        additionalInfo.put(key, value);
        return this;
    }

    public Response addAdditionalData(Object object) {
        this.data = object;
        return this;
    }


}
