package gaiden.da.guildservice.strategy;

import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.enums.GuildType;
import gaiden.da.guildservice.exceptions.GuildMustBeOpenException;

public class OnlyUponRequestJoinStrategy implements GuildJoinStrategy {
    @Override
    public void validateJoin(Guild guild, Long userId) {
        throw new GuildMustBeOpenException("This only upon request");
    }

    @Override
    public GuildType getGuildType() {
        return GuildType.ONLY_UPON_REQUEST;
    }
}
