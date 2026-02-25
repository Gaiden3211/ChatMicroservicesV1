package gaiden.da.chatservice.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gaiden.da.chatservice.config.RedisConfig;
import gaiden.da.chatservice.dto.AttachmentDto;
import gaiden.da.chatservice.dto.ChatMessageDto;
import gaiden.da.chatservice.entity.ChatMessage;
import gaiden.da.chatservice.repository.ChatRepository;
import gaiden.da.chatservice.service.ContactService;
import gaiden.da.grpc.guild.AccessRequest;
import gaiden.da.grpc.guild.AccessResponse;
import gaiden.da.grpc.guild.GuildServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;


import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    @GrpcClient("guild-service")
    private GuildServiceGrpc.GuildServiceBlockingStub guildServiceStub;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ContactService contactService;
    private final ChatRepository chatRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/chat/{guildId}/{channelId}/sendMessage")
    public void sendMessage(
            @DestinationVariable String guildId,
            @DestinationVariable String channelId,
            @Payload ChatMessageDto chatMessage,
            Principal principal
    ) {

        String userIdStr = principal.getName();
        log.info("User {} tries to send message to Guild {} Channel {}", userIdStr, guildId, channelId);

        try {
            AccessResponse response = guildServiceStub.checkGuildAccess(
                    AccessRequest.newBuilder()
                            .setUserId(userIdStr)
                            .setGuildId(guildId)
                            .build()
            );

            if (!response.getHasAccess()) {
                log.warn("⛔ Access DENIED for user {} in guild {}", userIdStr, guildId);
                return;
            }
        } catch (Exception e) {
            log.error("gRPC call failed", e);
            return;
        }

        chatMessage.setSender(userIdStr);
        chatMessage.setGuildId(guildId);
        chatMessage.setChannelId(channelId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setRecipientId(null);

        //СЕРІАЛІЗАЦІЯ: List -> JSON String
        String attachmentsJson = null;
        try {
            if (chatMessage.getAttachments() != null && !chatMessage.getAttachments().isEmpty()) {
                attachmentsJson = objectMapper.writeValueAsString(chatMessage.getAttachments());
            }
        } catch (Exception e) {
            log.error("Failed to serialize attachments", e);
        }


        ChatMessage savedMsg = chatRepository.save(ChatMessage.builder()
                .content(chatMessage.getContent())
                .sender(chatMessage.getSender())
                .guildId(guildId)
                .channelId(channelId)
                .timestamp(chatMessage.getTimestamp())
                .attachments(attachmentsJson)
                .build());

        chatMessage.setId(savedMsg.getId());


        String destination = String.format("/topic/guild/%s/channel/%s", guildId, channelId);
        log.info("📤 Publishing to Redis topic: {}", RedisConfig.CHAT_TOPIC);
        redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, chatMessage);
        log.info("✅ Message sent to {}", destination);
    }

    @GetMapping("/api/v1/chat/guild/{guildId}/{channelId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDto>> getGuildHistory(
            @PathVariable String guildId,
            @PathVariable String channelId
    ) {
        List<ChatMessage> messages = chatRepository.findByGuildIdAndChannelIdOrderByTimestampAsc(guildId, channelId);

        List<ChatMessageDto> dtos = messages.stream().map(msg -> {
            ChatMessageDto dto = new ChatMessageDto();
            dto.setId(msg.getId());
            dto.setContent(msg.getContent());
            dto.setSender(msg.getSender());
            dto.setGuildId(msg.getGuildId());
            dto.setChannelId(msg.getChannelId());
            dto.setTimestamp(msg.getTimestamp());


            if (msg.getAttachments() != null && !msg.getAttachments().isEmpty()) {
                try {
                    List<AttachmentDto> attachments = objectMapper.readValue(
                            msg.getAttachments(),
                            new TypeReference<List<AttachmentDto>>() {}
                    );
                    dto.setAttachments(attachments);
                } catch (Exception e) {
                    log.error("Failed to deserialize attachments for msg {}", msg.getId(), e);
                    dto.setAttachments(java.util.Collections.emptyList());
                }
            } else {
                dto.setAttachments(java.util.Collections.emptyList());
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @MessageMapping("/private/message")
    public void sendPrivateMessage(
            @Payload ChatMessageDto chatMessage,
            Principal principal
    ) {
        String senderId = principal.getName();
        String recipientId = chatMessage.getRecipientId();

        log.info("📩 Private message from User {} to User {} Content: {}", senderId, recipientId, chatMessage.getContent());


        contactService.ensureContactExists(senderId, recipientId);



        chatMessage.setSender(senderId);
        chatMessage.setTimestamp(LocalDateTime.now());



        redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, chatMessage);
    }






}