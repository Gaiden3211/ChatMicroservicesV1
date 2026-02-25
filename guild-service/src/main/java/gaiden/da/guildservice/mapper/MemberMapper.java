package gaiden.da.guildservice.mapper;

import gaiden.da.guildservice.domain.Member;
import gaiden.da.guildservice.dto.MemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RoleMapper.class})
public interface MemberMapper {


    MemberDto toDto(Member member);

    List<MemberDto> toDtos(List<Member> members);
}
