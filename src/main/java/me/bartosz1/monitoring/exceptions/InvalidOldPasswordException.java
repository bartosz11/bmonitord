package me.bartosz1.monitoring.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//subject-to-change - this code
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidOldPasswordException extends APIException {

    public InvalidOldPasswordException(String errorMessage) {
        super(errorMessage);
    }


}
