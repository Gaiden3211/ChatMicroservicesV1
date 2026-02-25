package gaiden.da.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gaiden.da.chatservice.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void onMessage(String messageJson) {
        try {
            ChatMessageDto chatMessage = objectMapper.readValue(messageJson, ChatMessageDto.class);

            if (chatMessage.getRecipientId() != null) {

                log.info("👤 Routing private message to User: {}", chatMessage.getRecipientId());


                messagingTemplate.convertAndSendToUser(
                        chatMessage.getRecipientId(),
                        "/queue/messages",
                        chatMessage
                );
            } else {

                log.info("📢 Routing guild message to Guild: {}", chatMessage.getGuildId());

                String destination = String.format("/topic/guild/%s/channel/%s",
                        chatMessage.getGuildId(),
                        chatMessage.getChannelId());

                messagingTemplate.convertAndSend(destination, chatMessage);
            }

        } catch (Exception e) {
            log.error("Failed to process message from Redis", e);
        }
    }
}