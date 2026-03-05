package gaiden.da.notificationservice.controller;

import gaiden.da.notificationservice.dto.PushSubscriptionRequest;
import gaiden.da.notificationservice.entity.PushSubscription;
import gaiden.da.notificationservice.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final PushSubscriptionRepository subscriptionRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PushSubscriptionRequest request) {

        log.info("📩 New push subscription received for user: {}", userId);

        // Перевіряємо, чи немає вже такої підписки
        if (subscriptionRepository.findByEndpoint(request.getEndpoint()).isEmpty()) {
            PushSubscription sub = new PushSubscription();
            sub.setUserId(userId.toString());
            sub.setEndpoint(request.getEndpoint());
            sub.setP256dh(request.getKeys().getP256dh());
            sub.setAuth(request.getKeys().getAuth());

            subscriptionRepository.save(sub);
            log.info("✅ Subscription saved successfully!");
        }

        return ResponseEntity.ok().build();
    }
}
