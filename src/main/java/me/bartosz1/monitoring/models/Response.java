package me.bartosz1.monitoring.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Response {

    private final int code;
    @JsonIgnore
    private final HttpStatus status;
    @JsonIncludeProperties({"message", "error", "code"})
    private final List<Object> errors = new ArrayList<>();
    private final HashMap<String, Object> additionalFields = new HashMap<>();
    private Object data;

    public Response(HttpStatus status) {
        this.code = status.value();
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public Object getData() {
        return data;
    }

    @JsonAnyGetter
    public HashMap<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public Response addError(Object error) {
        errors.add(error);
        return this;
    }

    public Response addAdditionalData(Object data) {
        this.data = data;
        return this;
    }

    public Response addAdditionalField(String key, Object value) {
        additionalFields.put(key, value);
        return this;
    }

    public ResponseEntity<Response> toResponseEntity() {
        return new ResponseEntity<>(this, this.status);
    }

}
