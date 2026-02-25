package gaiden.da.guildservice.exceptions;

public class GuildMustBeOpenException extends RuntimeException {
    public GuildMustBeOpenException(String message) {
        super(message);
    }
}
