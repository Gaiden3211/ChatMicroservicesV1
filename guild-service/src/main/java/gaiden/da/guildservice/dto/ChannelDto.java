package gaiden.da.guildservice.dto;

import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.enums.ChannelType;
import lombok.Data;

@Data
public class ChannelDto {

    private Long id;
    private String name;
    private ChannelType type;
    private Guild guild;

}
