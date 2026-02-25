package gaiden.da.guildservice.strategy;

import gaiden.da.guildservice.enums.GuildType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GuildJoinStrategyFactory {

    private final Map<GuildType, GuildJoinStrategy> strategies;

    // Spring сам передасть сюди PublicJoinStrategy, CloseJoinStrategy та OnlyUponRequestJoinStrategy
    public GuildJoinStrategyFactory(List<GuildJoinStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(GuildJoinStrategy::getGuildType, Function.identity()));
    }

    public GuildJoinStrategy getStrategy(GuildType type) {
        GuildJoinStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported guild type: " + type);
        }
        return strategy;
    }
}