package gaiden.da.authservice.exceptionHandler.hendler;


import gaiden.da.authservice.exceptionHandler.exceptions.RegistrationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class AuthGlobalExceptionHandler {

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<?> handleRegistrationException(RegistrationException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationExceptionException(AuthenticationException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        ex.printStackTrace();

        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<?> buildErrorResponse(Exception ex, HttpStatus status) {
        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(body, status);
    }
}
