package me.bartosz1.monitoring.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyTakenException extends APIException {

    public UsernameAlreadyTakenException(String errorMessage) {
        super(errorMessage);
    }

}
