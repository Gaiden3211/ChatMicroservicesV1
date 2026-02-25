package gaiden.da.authservice.exceptionHandler.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.CONFLICT)
public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }
}
