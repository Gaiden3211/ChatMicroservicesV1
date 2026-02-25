package gaiden.da.notificationservice.service;

import gaiden.da.notificationservice.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    private final JavaMailSender mailSender;

    // Беремо email відправника з конфігурації
    @Value("${spring.mail.username:discord.clone.bot@gmail.com}")
    private String senderEmail;

    public NotificationListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "user-registered-topic", groupId = "notification-group")
    public void handleUserRegistration(UserRegisteredEvent event) {
        log.info("📨 Received Kafka Event: User registered with email {}", event.getEmail());

        try {
            sendWelcomeEmail(event);
            log.info("✅ Welcome email successfully sent to {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", event.getEmail(), e.getMessage());
        }
    }

    private void sendWelcomeEmail(UserRegisteredEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(event.getEmail());
        message.setSubject("Welcome to Discord Clone! 🎉");

        // Формуємо тіло листа
        String body = String.format(
                "Привіт, %s!\n\n" +
                        "Дякуємо за реєстрацію в нашому застосунку.\n" +
                        "Твій унікальний ID: %s\n\n" +
                        "Приємного спілкування!\n" +
                        "З повагою, команда Discord Clone.",
                event.getUsername(), event.getUserId()
        );

        message.setText(body);

        // Відправляємо лист
        mailSender.send(message);
    }
}