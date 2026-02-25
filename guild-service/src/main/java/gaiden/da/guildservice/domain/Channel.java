package gaiden.da.guildservice.domain;

import gaiden.da.guildservice.enums.ChannelType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Guild guild;

}