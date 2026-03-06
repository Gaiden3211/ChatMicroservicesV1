package gaiden.da.notificationservice.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import gaiden.da.notificationservice.dto.PushNotificationEvent;
import gaiden.da.notificationservice.entity.PushSubscription;
import gaiden.da.notificationservice.repository.PushSubscriptionRepository;
import gaiden.da.notificationservice.service.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationKafkaListener {

    private final PushSubscriptionRepository subscriptionRepository;
    private final WebPushService webPushService;



    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "push-alerts", groupId = "notification-group-v2")
    public void handlePushEvent(String messageJson) {
        try {

            PushNotificationEvent event = objectMapper.readValue(messageJson, PushNotificationEvent.class);

            log.info("📨 Received push event for user: {}", event.getRecipientId());

            List<PushSubscription> subscriptions = subscriptionRepository.findAllByUserId(event.getRecipientId());

            if (subscriptions.isEmpty()) {
                log.info("🤷‍♂️ User {} has no push subscriptions.", event.getRecipientId());
                return;
            }

            for (PushSubscription sub : subscriptions) {
                webPushService.sendPush(sub, event.getTitle(), event.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Failed to parse PushNotificationEvent JSON: {}", messageJson, e);
        }
    }

}