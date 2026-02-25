package gaiden.da.guildservice.repository.interfaces;

import gaiden.da.guildservice.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGuildIdAndUserId(Long guildId, Long userId);
    boolean existsByGuildIdAndUserId(Long guildId, Long userId);
    Optional<Member> findByIdAndGuildId(Long memberId, Long guildId);
    List<Member> findAllByGuildId(Long guildId);

    List<Member> findAllByIdIn(List<Long> memberIds);
}

