package gaiden.da.chatservice.repository;

import gaiden.da.chatservice.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {


    List<Contact> findAllByOwnerIdOrderByLastInteractionDesc(String ownerId);


    boolean existsByOwnerIdAndPeerId(String ownerId, String peerId);

    Optional<Contact> findByOwnerIdAndPeerId(String ownerId, String peerId);
}