package gaiden.da.guildservice.mapper;

import gaiden.da.guildservice.domain.Channel;
import gaiden.da.guildservice.dto.ChannelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChannelMapper {

    @Mapping(target = "guild", ignore = true)
    ChannelDto toDto(Channel channel);

    List<ChannelDto> toDtos(List<Channel> channels);
}
