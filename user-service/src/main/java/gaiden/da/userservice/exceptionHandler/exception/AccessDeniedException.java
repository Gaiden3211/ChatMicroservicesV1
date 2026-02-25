package gaiden.da.userservice.exceptionHandler.exception;

public class AccessDeniedException extends RuntimeException {
  public AccessDeniedException(String message) {
    super(message);
  }
}
