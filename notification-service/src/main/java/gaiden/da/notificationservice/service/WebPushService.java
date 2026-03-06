package gaiden.da.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gaiden.da.notificationservice.entity.PushSubscription;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WebPushService {

    @Value("${webpush.vapid.public-key}")
    private String publicKey;
    @Value("${webpush.vapid.private-key}")
    private String privateKey;
    @Value("${webpush.vapid.subject}")
    private String subject;

    private PushService pushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() throws GeneralSecurityException {

        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey, subject);
    }

    public void sendPush(PushSubscription sub, String title, String body) {
        try {

            Subscription.Keys keys = new Subscription.Keys(sub.getP256dh(), sub.getAuth());
            Subscription subscription = new Subscription(sub.getEndpoint(), keys);


            Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("title", title);
            payloadMap.put("body", body);
            payloadMap.put("icon", "https://discord.com/assets/f9e7949365287f717887.png");

            String payload = objectMapper.writeValueAsString(payloadMap);


            Notification notification = new Notification(subscription, payload);
            pushService.send(notification);
            log.info("🚀 Push notification sent successfully to endpoint: {}", sub.getEndpoint());

        } catch (Exception e) {
            log.error("❌ Failed to send push notification", e);
        }
    }
}
