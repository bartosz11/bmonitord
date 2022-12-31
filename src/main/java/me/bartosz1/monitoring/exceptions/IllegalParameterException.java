package me.bartosz1.monitoring.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalParameterException extends APIException {

    public IllegalParameterException(String message) {
        super(message);
    }
}
