package gaiden.da.guildservice.domain;

import gaiden.da.guildservice.enums.Permission;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "guild_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"guild_id", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Guild guild;

    @Column(nullable = false)
    private Long userId;

    private String nickname;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "member_roles_link",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    private LocalDateTime joinedAt;

    public boolean hasPermission(Permission permission) {


        if (isOwner()) {
            return true;
        }


        return roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> p == Permission.ADMINISTRATOR || p == permission);
    }

    public boolean isOwner() {
        return guild.getOwnerId() != null
                && guild.getOwnerId().equals(userId);
    }

}
