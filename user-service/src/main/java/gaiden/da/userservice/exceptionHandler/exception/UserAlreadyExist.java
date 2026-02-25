package gaiden.da.userservice.exceptionHandler.exception;

public class UserAlreadyExist extends RuntimeException {
    public UserAlreadyExist(String message) {
        super(message);
    }
}
