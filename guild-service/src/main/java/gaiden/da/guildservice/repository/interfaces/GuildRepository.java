package gaiden.da.guildservice.repository.interfaces;

import gaiden.da.guildservice.domain.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
    boolean existsByName(String name);

    @Query("SELECT m.guild FROM Member m WHERE m.userId = :userId")
    List<Guild> findAllByUserId(@Param("userId") Long userId);

}
