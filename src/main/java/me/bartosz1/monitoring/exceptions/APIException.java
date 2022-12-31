package me.bartosz1.monitoring.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

public class APIException extends Exception {
    private final String message;

    public APIException(String message) {
        super(message);
        this.message = message;
    }

    public APIException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public APIException(Throwable cause) {
        super(cause);
        this.message = cause.getMessage();
    }

    public String getError() {
        return this.getClass().getSimpleName();
    }

    public int getCode() {
        return this.getClass().getAnnotation(ResponseStatus.class).value().value();
    }

    public String getMessage() {
        return message;
    }


}
