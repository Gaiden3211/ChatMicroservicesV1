package gaiden.da.authservice.exceptionHandler.exceptions;

import org.springframework.http.HttpStatus;

public class CustomFeignException extends RuntimeException {
    private final int status;

    public CustomFeignException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static class UserNotFound extends CustomFeignException {
        public UserNotFound(String message) {
            super(404, message);
        }
    }

    public static class UserAlreadyExists extends CustomFeignException {
        public UserAlreadyExists(String message) {
            super(409, message);
        }
    }

    public static class BadRequest extends CustomFeignException {
        public BadRequest(String message) {
            super(400, message);
        }
    }

}
