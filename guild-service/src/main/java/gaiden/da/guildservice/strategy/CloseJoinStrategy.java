package gaiden.da.guildservice.strategy;

import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.enums.GuildType;
import gaiden.da.guildservice.exceptions.GuildMustBeOpenException;

public class CloseJoinStrategy implements GuildJoinStrategy {
    @Override
    public void validateJoin(Guild guild, Long userId) {
        throw new GuildMustBeOpenException("This guild is a close guild. You should be invited");
    }

    @Override
    public GuildType getGuildType() {
        return GuildType.CLOSE;
    }
}
