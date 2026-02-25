package gaiden.da.guildservice.controller;


import gaiden.da.guildservice.dto.ChangeRolesRequestDto;
import gaiden.da.guildservice.dto.CreateRoleRequestDto;
import gaiden.da.guildservice.dto.RoleDto;
import gaiden.da.guildservice.service.GuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("api/v1/guilds/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles", description = "Roles control")
public class RoleController {
    private final GuildService guildService;

    @Operation(summary = "Assign role")
    @PostMapping("/{guildId}/assign")
    public ResponseEntity<Void> changeRole(@PathVariable Long guildId, @RequestBody ChangeRolesRequestDto roleRequestDTO, @RequestHeader("X-User-Id") Long adminUserId) {
        guildService.assignRoleToMember(guildId, roleRequestDTO, adminUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Create new role")
    @PostMapping("/{guildId}/create")
    public ResponseEntity<RoleDto> createRole(@PathVariable Long guildId, @RequestBody CreateRoleRequestDto roleRequestDTO, @RequestHeader("X-User-Id") Long userId) {
        RoleDto roleDto = guildService.createRole(guildId, userId, roleRequestDTO);
        return ResponseEntity.ok(roleDto);
    }
}
