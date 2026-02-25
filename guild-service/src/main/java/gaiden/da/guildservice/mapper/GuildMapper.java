package gaiden.da.guildservice.mapper;

import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.dto.GuildDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ChannelMapper.class, MemberMapper.class, RoleMapper.class})
public interface GuildMapper {

    GuildDto toDto(Guild guild);
    List<GuildDto> toDtos(List<Guild> guilds);
}
