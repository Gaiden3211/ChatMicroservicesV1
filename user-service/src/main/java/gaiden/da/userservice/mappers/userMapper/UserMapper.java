package gaiden.da.userservice.mappers.userMapper;

import gaiden.da.userservice.domain.User;
import gaiden.da.userservice.dto.UserCredentialDto;
import gaiden.da.userservice.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto toUserDto(User user);
    List<UserDto> toUserDtos(List<User> users);

    UserCredentialDto toUserCredentialDto(User user);

}
