package gaiden.da.chatservice.repository;

import gaiden.da.chatservice.entity.Contact;
import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends MongoRepository<Contact, Long> {


    List<Contact> findAllByOwnerIdOrderByLastInteractionDesc(String ownerId);


    boolean existsByOwnerIdAndPeerId(String ownerId, String peerId);

    Optional<Contact> findByOwnerIdAndPeerId(String ownerId, String peerId);

    List<Contact> findByOwnerId(String ownerId);
}