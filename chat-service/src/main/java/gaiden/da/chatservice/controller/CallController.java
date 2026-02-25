package gaiden.da.chatservice.controller;

import gaiden.da.chatservice.dto.CallSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/private/call")
    public void handleCallSignal(@Payload CallSignal signal) {
        log.info("📞 Signal {} from {} to {}", signal.getType(), signal.getSenderId(), signal.getRecipientId());


        messagingTemplate.convertAndSendToUser(
                String.valueOf(signal.getRecipientId()),
                "/queue/call",
                signal
        );
    }
}