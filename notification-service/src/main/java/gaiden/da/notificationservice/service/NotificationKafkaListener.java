package gaiden.da.notificationservice.service;
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

    // Слухаємо топік push-notifications
    @KafkaListener(topics = "push-notifications", groupId = "notification-group")
    public void handlePushEvent(PushNotificationEvent event) {
        log.info("📨 Received push event for user: {}", event.getRecipientId());

        // Дістаємо всі підписки юзера (наприклад, з ПК і з телефона)
        List<PushSubscription> subscriptions = subscriptionRepository.findAllByUserId(event.getRecipientId());

        if (subscriptions.isEmpty()) {
            log.info("🤷‍♂️ User {} has no push subscriptions.", event.getRecipientId());
            return;
        }

        // Шлемо пуші на всі його пристрої
        for (PushSubscription sub : subscriptions) {
            webPushService.sendPush(sub, event.getTitle(), event.getBody());
        }
    }
}
