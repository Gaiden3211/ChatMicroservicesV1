package gaiden.da.chatservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "peer_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "peer_id", nullable = false)
    private String peerId;

    @Column(name = "last_interaction")
    private LocalDateTime lastInteraction;
}