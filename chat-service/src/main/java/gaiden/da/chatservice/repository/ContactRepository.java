package gaiden.da.chatservice.repository;

import gaiden.da.chatservice.entity.Contact;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {


    List<Contact> findAllByOwnerIdOrderByLastInteractionDesc(String ownerId);


    boolean existsByOwnerIdAndPeerId(String ownerId, String peerId);

    Optional<Contact> findByOwnerIdAndPeerId(String ownerId, String peerId);

    @Query("SELECT c.peerId FROM Contact c WHERE c.ownerId = :userId")
    List<String> findContactIdsByUserId(@Param("userId") String userId);
}