package gaiden.da.authservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


public record UserResponse (
        Long id,
        String username,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Long>ownGuild
)
{}

