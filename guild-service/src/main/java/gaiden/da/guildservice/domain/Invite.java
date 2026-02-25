package gaiden.da.guildservice.domain;

import gaiden.da.guildservice.enums.InviteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invites")
@EntityListeners(AuditingEntityListener.class)
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    private Guild guild;

    @Column(nullable = false)
    private Long inviterId;

    @Column(nullable = false)
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
}
