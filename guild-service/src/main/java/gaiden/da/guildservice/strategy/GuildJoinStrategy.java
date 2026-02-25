package gaiden.da.guildservice.strategy;

import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.enums.GuildType;

public interface GuildJoinStrategy {

    void validateJoin(Guild guild, Long userId);

    // Spring использует этот метод, чтобы понять, для какого типа гильдии эта стратегия
    GuildType getGuildType();

}
