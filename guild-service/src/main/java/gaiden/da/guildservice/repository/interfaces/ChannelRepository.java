package gaiden.da.guildservice.repository.interfaces;

import gaiden.da.guildservice.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

}
