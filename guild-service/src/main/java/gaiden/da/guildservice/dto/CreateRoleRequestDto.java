package gaiden.da.guildservice.dto;

import gaiden.da.guildservice.enums.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequestDto {
    private String name;
    private String color;
    private Set<Permission> permissions;
}
