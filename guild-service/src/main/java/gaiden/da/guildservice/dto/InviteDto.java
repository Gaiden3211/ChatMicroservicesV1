package gaiden.da.guildservice.dto;

import gaiden.da.guildservice.enums.InviteStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteDto {
    private Long id;
    private InviteStatus status;
    private Long inviterId;
    private Long guildId;
    private String guildName;
    private String guildIconUrl;
}
