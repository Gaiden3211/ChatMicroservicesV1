package gaiden.da.userservice.exceptionHandler.handler;

import gaiden.da.userservice.dto.UserDto;
import gaiden.da.userservice.exceptionHandler.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandlers {

    @ExceptionHandler(UserNotFoundById.class)
    public ResponseEntity<String> handleUserNotFoundById(UserNotFoundById ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserIDWasNotProvided.class)
    public ResponseEntity<String> handleUserIDWasNotProvided(UserIDWasNotProvided ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotSpecified.class)
    public ResponseEntity<String> handleUsernameNotSpecified(UsernameNotSpecified ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotSpecified.class)
    public ResponseEntity<String> handleEmailNotSpecified(EmailNotSpecified ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<String> handleUserAlreadyExist(UserAlreadyExist ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        System.err.println("Unhandled exception occurred: " + ex.getMessage());
        Map<String, Object> body = Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "message", "An unexpected error occurred on the server."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
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
