package gaiden.da.userservice.exceptionHandler.exception;

public class UserIDWasNotProvided extends RuntimeException {
    public UserIDWasNotProvided(String message) {
        super(message);
    }
}
