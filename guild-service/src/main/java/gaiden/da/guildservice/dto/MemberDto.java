package gaiden.da.guildservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class MemberDto {
    private Long id;
    private Long userId;
    private String nickname;
    private Set<RoleDto> roles;
    private LocalDateTime joinedAt;

}
