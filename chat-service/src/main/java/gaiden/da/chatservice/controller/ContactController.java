package gaiden.da.chatservice.controller;

import gaiden.da.chatservice.entity.Contact;
import gaiden.da.chatservice.service.ContactService;
import gaiden.da.chatservice.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final UserPresenceService presenceService;


    @GetMapping
    public ResponseEntity<List<String>> getMyContacts(@RequestHeader("X-User-Id") String userId) {
        List<Contact> contacts = contactService.getMyContacts(userId);

        List<String> peerIds = contacts.stream()
                .map(Contact::getPeerId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(peerIds);
    }

    @GetMapping("/user/{userId}/ids")
    public ResponseEntity<List<String>> getUserContactIds(@PathVariable String userId) {

        // Дістаємо з БД ID всіх друзів (з ким є активні чати/контакти)
        List<String> contactIds = contactService.getContactIdsForUser(userId);

        return ResponseEntity.ok(contactIds);
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, String>> checkStatuses(@RequestBody List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> statuses = presenceService.getStatuses(userIds);
        return ResponseEntity.ok(statuses);
    }
}