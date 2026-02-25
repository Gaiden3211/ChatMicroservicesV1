package gaiden.da.guildservice.repository.interfaces;

import gaiden.da.guildservice.domain.Invite;
import gaiden.da.guildservice.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    boolean existsByGuildIdAndTargetUserIdAndStatus(Long guild_id, Long targetUserId, InviteStatus status);

    List<Invite> findAllByTargetUserIdAndStatus(Long targetUserId, InviteStatus status);
}
