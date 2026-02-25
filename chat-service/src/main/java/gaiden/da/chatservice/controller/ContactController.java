package gaiden.da.chatservice.controller;

import gaiden.da.chatservice.entity.Contact;
import gaiden.da.chatservice.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;


    @GetMapping
    public ResponseEntity<List<String>> getMyContacts(@RequestHeader("X-User-Id") String userId) {
        List<Contact> contacts = contactService.getMyContacts(userId);

        List<String> peerIds = contacts.stream()
                .map(Contact::getPeerId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(peerIds);
    }
}