package me.bartosz1.monitoring;

import me.bartosz1.monitoring.models.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionResolver extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionResolver.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> doResolveException(HttpServletRequest request, Exception ex) {
        try {
            ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
            HttpStatus status;
            if (annotation != null) status = annotation.value();
            else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                //subject-to-change
                ex.printStackTrace();
            }
            Response responseBody = new Response(status).addError(ex);
            return responseBody.toResponseEntity();
        } catch (Exception exception) {
            LOGGER.warn("Handling " + ex.getClass().getSimpleName() + " on request from " + request.getRemoteAddr() + " resulted in " + exception.getMessage());
        }
        return null;
    }

}
