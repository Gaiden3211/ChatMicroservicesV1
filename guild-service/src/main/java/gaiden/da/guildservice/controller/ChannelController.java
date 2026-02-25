package gaiden.da.guildservice.controller;

import gaiden.da.guildservice.dto.ChannelDto;
import gaiden.da.guildservice.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/guild/{guildId}/channel")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Channels", description = "Channels control")
public class ChannelController {
    private final ChannelService channelService;

    @Operation(summary = "Create channel")
    @PostMapping("/create")
    public ResponseEntity<ChannelDto> createChannel(@PathVariable("guildId") Long guildId,
                                                    @RequestHeader("X-User-Id") Long userId ,
                                                    @RequestBody ChannelDto channelDto
    ) {
        log.info("Creating channel '{}' by member ID: {}", channelDto.getName(), userId);

        ChannelDto createdChannel = channelService.create(guildId, userId, channelDto);

        return new ResponseEntity<>(createdChannel, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete channel")
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable("guildId") Long guildId,
                                              @RequestHeader("X-User-Id") Long userId ,
                                              @PathVariable("channelId") Long channelId

    ) {
        channelService.delete(guildId, channelId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
