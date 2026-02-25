package gaiden.da.guildservice.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandlers {

    @ExceptionHandler(GuildAlreadyExistsException.class)
    public ResponseEntity<String> handleGuildAlreadyExistsException(GuildAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GuildNotFoundException.class)
    public ResponseEntity<String> handleGuildNotFoundException(GuildNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFoundException(MemberNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotRightsException.class)
    public ResponseEntity<String> handleNotRightsException(NotRightsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MemberAlreadyExistInGuildException.class)
    public ResponseEntity<String> handleMemberAlreadyExistInGuildException(MemberAlreadyExistInGuildException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GuildMustBeOpenException.class)
    public ResponseEntity<String> handleGuildMustBeOpenException(GuildMustBeOpenException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InviteAlreadyExistsException.class)
    public ResponseEntity<String> handleInviteAlreadyExistsException(InviteAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<String> handleChannelNotFoundException(ChannelNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

}

