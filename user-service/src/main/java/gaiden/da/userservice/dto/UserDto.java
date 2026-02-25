package gaiden.da.userservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class UserDto {
    private Long id;


    private String username;
    private String email;

    private String avatarUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Long> ownGuildIds;

    private String publicKey;

}
