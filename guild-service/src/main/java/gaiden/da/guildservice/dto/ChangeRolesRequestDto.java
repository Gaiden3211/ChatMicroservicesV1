package gaiden.da.guildservice.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ChangeRolesRequestDto {
    private Long memberId;
    private Set<Long> roleIds;
}
