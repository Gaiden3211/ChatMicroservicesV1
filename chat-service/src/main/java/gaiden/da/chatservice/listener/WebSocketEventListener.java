package gaiden.da.chatservice.listener;

import gaiden.da.chatservice.dto.UserStatusEvent;
import gaiden.da.chatservice.service.ContactService;
import gaiden.da.chatservice.service.UserPresenceService;
import gaiden.da.chatservice.service.UserStatusBroadcastService;
import gaiden.da.grpc.guild.GuildServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserPresenceService presenceService;

    private final UserStatusBroadcastService broadcastService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        List<String> userIdHeaders = accessor.getNativeHeader("userId");

        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
            String userId = userIdHeaders.get(0);

            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("userId", userId);
            }

            log.info("🟢 User ONLINE: {}", userId);
            presenceService.setOnline(userId);

            broadcastService.broadcastStatus(userId, "ONLINE");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
            String userId = (String) sessionAttributes.get("userId");

            log.info("🔴 User OFFLINE: {}", userId);
            presenceService.setOffline(userId);

            broadcastService.broadcastStatus(userId, "OFFLINE");
        }
    }
}

