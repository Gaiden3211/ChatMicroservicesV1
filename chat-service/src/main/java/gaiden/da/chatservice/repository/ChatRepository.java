package gaiden.da.chatservice.repository;


import gaiden.da.chatservice.entity.ChatMessage;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByGuildIdAndChannelIdOrderByTimestampAsc(String guildId, String channelId);

    List<ChatMessage> findBySenderAndRecipientIdOrSenderAndRecipientIdOrderByTimestampAsc(
            String sender1, String recipient1, String sender2, String recipient2
    );
}