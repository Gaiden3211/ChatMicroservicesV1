package gaiden.da.chatservice.controller;

import gaiden.da.chatservice.config.RedisConfig;
import gaiden.da.chatservice.dto.ChatMessageDto;
import gaiden.da.chatservice.dto.PushNotificationEvent;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

        // 🔥 Магія Mongo: ми кладемо списки та об'єкти прямо в сутність, ніякої серіалізації!
        ChatMessage savedMsg = chatRepository.save(ChatMessage.builder()
                .content(chatMessage.getContent())
                .sender(chatMessage.getSender())
                .guildId(guildId)
                .channelId(channelId)
                .timestamp(chatMessage.getTimestamp())
                .attachments(chatMessage.getAttachments() != null ? chatMessage.getAttachments() : new ArrayList<>())
                .reactions(new HashMap<>())
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

            // 🔥 MongoDB сама все розпарсила! Просто передаємо об'єкти в DTO
            dto.setAttachments(msg.getAttachments() != null ? msg.getAttachments() : new ArrayList<>());
            dto.setReactions(msg.getReactions() != null ? msg.getReactions() : new HashMap<>());

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

        String title = "Нове повідомлення від юзера " + chatMessage.getSender();
        String body = "У вас нове повідомлення у приватному чаті!";

        chatMessage.setSender(senderId);
        chatMessage.setTimestamp(LocalDateTime.now());

        redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, chatMessage);
        PushNotificationEvent pushEvent = new PushNotificationEvent(chatMessage.getRecipientId(), title, body);

        kafkaTemplate.send("push-alerts", pushEvent);
    }

    @MessageMapping("/chat/{guildId}/{channelId}/action")
    public void handleGuildMessageAction(
            @DestinationVariable String guildId,
            @DestinationVariable String channelId,
            @Payload ChatMessageDto actionDto,
            Principal principal
    ) {
        String userIdStr = principal.getName();
        actionDto.setSender(userIdStr);
        actionDto.setGuildId(guildId);
        actionDto.setChannelId(channelId);

        // 🔥 В MongoDB ID це String, тому Long.parseLong більше не потрібен!
        String msgId = actionDto.getId();

        chatRepository.findById(msgId).ifPresent(msg -> {

            if ("EDIT".equals(actionDto.getAction())) {
                if (!msg.getSender().equals(userIdStr)) {
                    log.warn("User {} tried to edit someone else's message {}", userIdStr, msgId);
                    return;
                }
                msg.setContent(actionDto.getContent());
                chatRepository.save(msg);

            } else if ("DELETE".equals(actionDto.getAction())) {
                if (!msg.getSender().equals(userIdStr)) {
                    log.warn("User {} tried to delete someone else's message {}", userIdStr, msgId);
                    return;
                }
                chatRepository.delete(msg);

            } else if ("REACT".equals(actionDto.getAction())) {
                // Смайлики можуть ставити ВСІ, тому перевірки на автора тут немає
                String emoji = actionDto.getContent();

                // Беремо мапу реакцій прямо з об'єкта (без ObjectMapper!)
                Map<String, Set<String>> reactionMap = msg.getReactions();
                if (reactionMap == null) {
                    reactionMap = new HashMap<>();
                }

                Set<String> users = reactionMap.computeIfAbsent(emoji, k -> new HashSet<>());

                // Toggle логіка (якщо є - видаляємо, якщо немає - додаємо)
                if (users.contains(userIdStr)) {
                    users.remove(userIdStr);
                    if (users.isEmpty()) {
                        reactionMap.remove(emoji);
                    }
                } else {
                    users.add(userIdStr);
                }

                msg.setReactions(reactionMap);
                chatRepository.save(msg);

                // Оновлюємо DTO, щоб розіслати правильний стан усім
                actionDto.setReactions(reactionMap);
            }

            // Розсилаємо оновлення всім у кімнаті
            redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, actionDto);
        });
    }

    @MessageMapping("/private/action")
    public void handlePrivateMessageAction(
            @Payload ChatMessageDto actionDto,
            Principal principal
    ) {
        String senderId = principal.getName();
        actionDto.setSender(senderId);

        log.info("📩 Private action {} from User {} to User {}", actionDto.getAction(), senderId, actionDto.getRecipientId());

        redisTemplate.convertAndSend(RedisConfig.CHAT_TOPIC, actionDto);
    }
}