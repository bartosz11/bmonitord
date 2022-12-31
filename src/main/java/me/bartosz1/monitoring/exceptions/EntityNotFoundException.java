package me.bartosz1.monitoring.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends APIException {

    public EntityNotFoundException(String message) {
        super(message);
    }

}
