package gaiden.da.guildservice.controller;

import gaiden.da.guildservice.domain.Invite;
import gaiden.da.guildservice.dto.InviteDto;
import gaiden.da.guildservice.service.GuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invites")
@RequiredArgsConstructor
@Tag(name = "Invites", description = "Invites control")
public class InviteController {

    private final GuildService guildService;

    @Operation(summary = "Get my invites")
    @GetMapping
    public ResponseEntity<List<InviteDto>> getMyInvites(@RequestHeader("X-User-Id") Long userId) {
       List<InviteDto> invites = guildService.getMyInvites(userId);
        return ResponseEntity.ok(invites);
    }

    @Operation(summary = "Accept invite")
    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<Void> acceptInvite(
            @PathVariable Long inviteId,
            @RequestHeader("X-User-Id") Long userId
    ) {

        guildService.acceptInvite(inviteId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Decline invite")
    @PostMapping("/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(
            @PathVariable Long inviteId,
            @RequestHeader("X-User-Id") Long userId
    ) {

        guildService.declineInvite(inviteId, userId);
        return ResponseEntity.noContent().build();
    }



}
