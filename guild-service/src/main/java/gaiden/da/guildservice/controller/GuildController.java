package gaiden.da.guildservice.controller;

import gaiden.da.guildservice.dto.CreateGuildRequest;
import gaiden.da.guildservice.dto.GuildDto;
import gaiden.da.guildservice.dto.MemberDto;
import gaiden.da.guildservice.dto.SendInviteRequest;
import gaiden.da.guildservice.service.GuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Guilds", description = "Guild control")
public class GuildController {

    private final GuildService guildService;



    @GetMapping("/user/{userId}/ids")
    public ResponseEntity<List<String>> getUserGuildIds(@PathVariable String userId) {


        List<String> guildIds = guildService.getGuildIdsForUser(userId);

        return ResponseEntity.ok(guildIds);
    }

    @Operation(summary = "Create guild", description = "Creates a new guild and becomes the founder of Authority")
    @PostMapping
    public ResponseEntity<GuildDto> createGuild(@RequestBody CreateGuildRequest request,
                                                @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Creating guild '{}' for user ID: {}", request.getName(), userId);

        GuildDto createdGuild = guildService.createGuild(
                request.getName(),
                request.getIconUrl(),
                userId
        );

        return ResponseEntity.ok(createdGuild);
    }

    @Operation(summary = "Get guild")
    @GetMapping("/{id}")
    public ResponseEntity<GuildDto> getGuild(@PathVariable Long id) {
        return ResponseEntity.ok(guildService.getGuild(id));
    }

    @Operation(summary = "Get members", description = "Get info about members in guild")
    @GetMapping ("/{id}/allMembers")
    public ResponseEntity<List<MemberDto>> getAllMembers(@PathVariable Long id) {
        return ResponseEntity.ok(guildService.getAllMembers(id));
    }

    @Operation(summary = "Delete guild")
    @DeleteMapping("/{guildId}")
    public ResponseEntity<Void> deleteGuild(@PathVariable Long guildId,
                                            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId
    ) {
        guildService.delete(guildId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Join to the guild")
    @PostMapping("/{guildId}/join")
    public ResponseEntity<Void> joinToTheGuild(@PathVariable Long guildId,
                                               @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId
    ) {
        guildService.joinMember(guildId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Send invite")
    @PostMapping("/invite/{guildId}/{newMemberId}")
    public ResponseEntity<Void> addToTheGuild(@PathVariable Long guildId,
                                              @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
                                              @RequestBody SendInviteRequest request
    ) {
        guildService.sendInvite(guildId, userId, request.getTargetUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Delete from guild")
    @DeleteMapping("/{guildId}/{memberId}")
            public ResponseEntity<Void> deleteFromGuild(@PathVariable Long memberId,
                                                        @PathVariable Long guildId,
                                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId
    ) {
        guildService.deleteMember(guildId,memberId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Leave from guild")
    @DeleteMapping("/{guildId}/me")
    public ResponseEntity<Void> outFromTheGuild (@PathVariable Long guildId,
                                                 @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId
    ){
        guildService.leaveServer(guildId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Transfer ownership")
    @PostMapping("/{guildId}/{memberId}/transfer_ownership")
    public ResponseEntity<GuildDto> transferOfOwnership(@PathVariable Long guildId,
                                                        @PathVariable Long memberId,
                                                        @Parameter(hidden = true) @RequestHeader("X-User-Id") Long oldOwnerId
    ){
       GuildDto guildDto = guildService.transferOfOwnership(guildId, memberId, oldOwnerId);
       return ResponseEntity.ok(guildDto);
    }

    @Operation(summary = "Get my guilds")
    @GetMapping("/me")
    public ResponseEntity<List<GuildDto>> getMyGuilds(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        List<GuildDto> myGuilds = guildService.getMyGuilds(userId);
        return ResponseEntity.ok(myGuilds);
    }


}
