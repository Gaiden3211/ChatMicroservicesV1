package gaiden.da.guildservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GuildDto {
    private Long id;
    private String name;
    private String iconUrl;
    private Long ownerId;
    private List<ChannelDto> channels;
    private LocalDateTime createdAt;
}
