package gaiden.da.chatservice.repository;


import gaiden.da.chatservice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByGuildIdAndChannelIdOrderByTimestampAsc(String guildId, String channelId);

    List<ChatMessage> findBySenderAndRecipientIdOrSenderAndRecipientIdOrderByTimestampAsc(
            String sender1, String recipient1, String sender2, String recipient2
    );
}