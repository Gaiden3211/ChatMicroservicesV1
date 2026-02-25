package gaiden.da.guildservice.exceptions;

public class MemberAlreadyExistInGuildException extends RuntimeException {
    public MemberAlreadyExistInGuildException(String message) {
        super(message);
    }
}
