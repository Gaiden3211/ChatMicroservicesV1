package gaiden.da.guildservice.service;

import gaiden.da.guildservice.domain.Channel;
import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.domain.Member;
import gaiden.da.guildservice.dto.ChannelDto;
import gaiden.da.guildservice.enums.Permission;
import gaiden.da.guildservice.exceptions.ChannelNotFoundException;
import gaiden.da.guildservice.exceptions.GuildNotFoundException;
import gaiden.da.guildservice.exceptions.MemberNotFoundException;
import gaiden.da.guildservice.exceptions.NotRightsException;
import gaiden.da.guildservice.mapper.ChannelMapper;
import gaiden.da.guildservice.repository.interfaces.ChannelRepository;
import gaiden.da.guildservice.repository.interfaces.GuildRepository;
import gaiden.da.guildservice.repository.interfaces.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final MemberRepository memberRepository;
    private final GuildRepository guildRepository;
    private final ChannelMapper channelMapper;

    @Transactional
    public ChannelDto create(Long guildId, Long userId, ChannelDto channelDto) {
        Member member = memberRepository.findByGuildIdAndUserId(guildId, userId).orElseThrow(() -> new MemberNotFoundException("User is not a member of this guild"));
        Guild guild = guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException("Guild not found with id: " + guildId));

        if (guild.getOwnerId().equals(userId)) {
            Channel channel = Channel.builder()
                    .guild(guild)
                    .name(channelDto.getName())
                    .type(channelDto.getType())
                    .build();
            channelRepository.save(channel);

            return channelMapper.toDto(channel);
        }

        boolean isAdmin = member.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission == Permission.ADMINISTRATOR || permission == Permission.MANAGE_CHANNELS);

        if (!isAdmin) {
            throw new NotRightsException("You don't have enough rights");
        }

        Channel channel = Channel.builder()
                .guild(guild)
                .name(channelDto.getName())
                .type(channelDto.getType())
                .build();
        channelRepository.save(channel);

        return channelMapper.toDto(channel);

    }

    @Transactional
    public void delete(Long guildId, Long channelId, Long userId) {
        Member member = memberRepository.findByGuildIdAndUserId(guildId, userId).orElseThrow(() -> new MemberNotFoundException("User is not a member of this guild"));
        Guild guild = guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException("Guild not found with id: " + guildId));
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));

        if (guild.getOwnerId().equals(userId)) {
            channelRepository.delete(channel);
            return;
        }

        boolean isAdmin = member.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission == Permission.ADMINISTRATOR || permission == Permission.MANAGE_CHANNELS);

        if (!isAdmin) {
            throw new NotRightsException("You don't have enough rights");
        }

        channelRepository.delete(channel);
        

    }
}
