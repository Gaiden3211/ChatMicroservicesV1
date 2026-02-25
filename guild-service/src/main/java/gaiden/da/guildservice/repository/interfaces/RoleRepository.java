package gaiden.da.guildservice.repository.interfaces;

import gaiden.da.guildservice.domain.Member;
import gaiden.da.guildservice.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByNameAndGuildId(String s, Long guildId);
    Optional<Role> findByIdAndGuildId(Long roleId, Long guildId);

    boolean existsByGuildIdAndNameIgnoreCase(Long guildId, String name);

    List<Role> findAllByIdInAndGuildId(Set<Long> roleIds, Long guildId);
}
