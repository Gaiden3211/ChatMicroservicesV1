package gaiden.da.guildservice.mapper;

import gaiden.da.guildservice.domain.Role;
import gaiden.da.guildservice.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {

    RoleDto toDto(Role role);

    List<RoleDto> toDtos(List<Role> roles);
}
