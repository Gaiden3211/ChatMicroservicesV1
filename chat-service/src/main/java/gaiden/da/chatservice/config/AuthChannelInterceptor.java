package gaiden.da.chatservice.config;

import gaiden.da.chatservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtils.isTokenValid(token)) {
                    String userId = jwtUtils.extractClaim(token, claims -> String.valueOf(claims.get("userId")));


                    if (userId == null || "null".equals(userId)) {
                        log.error("⛔ Token is valid but 'userId' claim is missing!");
                        return message;
                    }
                    log.info("🔓 Auth success. Raw ID from token: {}", userId);


                    UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                            userId, // Тепер principal.getName() поверне число (у вигляді рядка)
                            null,
                            List.of()
                    );

                    accessor.setUser(user);
                } else {
                    log.warn("⛔ Invalid Token");
                }
            } else {
                log.warn("⛔ No Token found in STOMP header");
            }
        }
        return message;
    }
}