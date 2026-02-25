package gaiden.da.userservice.exceptionHandler.exception;

public class UserNotFoundById extends RuntimeException {
    public UserNotFoundById(String message) {
        super(message);
    }
}
