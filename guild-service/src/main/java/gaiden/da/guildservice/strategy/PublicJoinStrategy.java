package gaiden.da.guildservice.strategy;

import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.enums.GuildType;
import org.springframework.stereotype.Component;

@Component
public class PublicJoinStrategy implements GuildJoinStrategy {


    @Override
    public GuildType getGuildType() {
        return GuildType.PUBLIC;
    }

    @Override
    public void validateJoin(Guild guild, Long userId) {
        // Усе добре, прямий вхід дозволено, нічого не кидаємо
    }


}
