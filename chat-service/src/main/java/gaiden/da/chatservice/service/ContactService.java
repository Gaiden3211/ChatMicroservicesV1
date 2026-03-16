package gaiden.da.chatservice.service;

import gaiden.da.chatservice.entity.Contact;
import gaiden.da.chatservice.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;


//    @Transactional
    public void ensureContactExists(String senderId, String recipientId) {
        updateOrSave(senderId, recipientId);
        updateOrSave(recipientId, senderId);
    }

    private void updateOrSave(String ownerId, String peerId) {
        contactRepository.findByOwnerIdAndPeerId(ownerId, peerId)
                .ifPresentOrElse(
                        existingContact -> {
                            existingContact.setLastInteraction(LocalDateTime.now());
                            contactRepository.save(existingContact);
                            // log.debug("🔄 Updated interaction time for User {} -> User {}", ownerId, peerId);
                        },
                        () -> {
                            Contact newContact = Contact.builder()
                                    .ownerId(ownerId)
                                    .peerId(peerId)
                                    .lastInteraction(LocalDateTime.now())
                                    .build();
                            contactRepository.save(newContact);
                            log.info("📝 Created new contact link: User {} -> User {}", ownerId, peerId);
                        }
                );
    }

    public List<Contact> getMyContacts(String userId) {
        return contactRepository.findAllByOwnerIdOrderByLastInteractionDesc(userId);
    }

    public List<String> getContactIdsForUser(String userId) {
        return contactRepository.findByOwnerId(userId)
                .stream()
                .map(Contact::getPeerId)
                .toList();
    }
}