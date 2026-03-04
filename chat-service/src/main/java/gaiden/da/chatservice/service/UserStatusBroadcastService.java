package gaiden.da.chatservice.service;


import gaiden.da.chatservice.dto.UserStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStatusBroadcastService {

    private final SimpMessageSendingOperations messagingTemplate;
    private final RestTemplate restTemplate;
    private final ContactService contactService;

    @Value("${GUILD_SERVICE_URL:http://localhost:8083}")
    private String guildServiceUrl;

    @Value("${USER_SERVICE_URL:http://localhost:8080}")
    private String userServiceUrl;

    @Async
    public void broadcastStatus(String userId, String status) {
        UserStatusEvent statusEvent = new UserStatusEvent(userId, status);

        broadcastToGuilds(userId, statusEvent);
        broadcastToContacts(userId, statusEvent);
    }

    private void broadcastToContacts(String userId, UserStatusEvent statusEvent) {
        try {
            List<String> contactIds = contactService.getContactIdsForUser(userId);

            if (contactIds != null) {
                for (String contactId : contactIds) {
                    messagingTemplate.convertAndSendToUser(contactId, "/queue/status", statusEvent);
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to broadcast to contacts for user: {}", userId, e);
        }
    }


    private void broadcastToGuilds(String userId, UserStatusEvent statusEvent) {
        try {
            String url = guildServiceUrl + "/api/v1/guilds/user/" + userId + "/ids";
            String[] guildIds = restTemplate.getForObject(url, String[].class);

            if (guildIds != null) {
                for (String guildId : guildIds) {
                    messagingTemplate.convertAndSend("/topic/guild/" + guildId + "/status", statusEvent);
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to fetch guilds via HTTP for user: {}", userId, e);
        }
    }

}

