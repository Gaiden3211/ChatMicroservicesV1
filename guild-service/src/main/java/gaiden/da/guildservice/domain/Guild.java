package gaiden.da.guildservice.domain;

import gaiden.da.guildservice.enums.GuildType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "guilds")
@EntityListeners(AuditingEntityListener.class)
public class Guild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String iconUrl;

    @Column(nullable = false)
    private Long ownerId;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Channel> channels = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Role> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GuildType type = GuildType.PUBLIC;
}
