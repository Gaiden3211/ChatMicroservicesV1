package gaiden.da.guildservice.service;

//import gaiden.da.guildservice.client.UserGrpcClient;
import gaiden.da.guildservice.domain.*;
import gaiden.da.guildservice.dto.*;
import gaiden.da.guildservice.enums.ChannelType;
import gaiden.da.guildservice.enums.GuildType;
import gaiden.da.guildservice.enums.InviteStatus;
import gaiden.da.guildservice.enums.Permission;
import gaiden.da.guildservice.exceptions.*;
import gaiden.da.guildservice.mapper.GuildMapper;
import gaiden.da.guildservice.mapper.RoleMapper;
import gaiden.da.guildservice.repository.interfaces.*;
import gaiden.da.guildservice.strategy.GuildJoinStrategy;
import gaiden.da.guildservice.strategy.GuildJoinStrategyFactory;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuildService {

    private final GuildJoinStrategyFactory joinStrategyFactory;
    private final GuildRepository guildRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final GuildMapper guildMapper;
    private final InviteRepository inviteRepository;
    private final RoleMapper roleMapper;


    @Transactional
    public GuildDto createGuild(String name, String iconUrl, Long ownerId) {


        if (guildRepository.existsByName(name)) {
            throw new GuildAlreadyExistsException("Guild with name '" + name + "' already exists");

        }


        Guild guild = Guild.builder()
                .name(name)
                .iconUrl(iconUrl)
                .ownerId(ownerId)
                .build();

        Role everyoneRole = Role.builder()
                .name("@everyone")
                .color("#99AAB5") // Стандартний сірий колір Discord
                .guild(guild)
                .permissions(new HashSet<>(Set.of(
                        Permission.SEND_MESSAGES,
                        Permission.READ_MESSAGES,
                        Permission.CONNECT_VOICE
                )))
                .build();


        Member ownerMember = Member.builder()
                .userId(ownerId)
                .guild(guild)
                .nickname(null)
                .roles(Set.of(everyoneRole))
                .build();

        guild.setRoles(new ArrayList<>(List.of(everyoneRole)));


//        guild.setMembers(new ArrayList<>(List.of(ownerMember)));



        Channel generalText = Channel.builder()
                .name("general")
                .type(ChannelType.TEXT)
                .guild(guild)
                .build();

        Channel generalVoice = Channel.builder()
                .name("General")
                .type(ChannelType.VOICE)
                .guild(guild)
                .build();

        guild.getChannels().add(generalText);
        guild.getChannels().add(generalVoice);

        Guild savedGuild = guildRepository.save(guild);
        ownerMember.setGuild(savedGuild);

        memberRepository.save(ownerMember);

        return guildMapper.toDto(savedGuild);
    }

    public GuildDto getGuild(Long id) {
        Guild guild = guildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guild not found with id: " + id));
        return guildMapper.toDto(guild);
    }

    public void delete(Long guildId, Long userId) {
        Guild guildToDelete = guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException("Guild not found with id: " + guildId));
        Member memberFromGuildToDelete = memberRepository.findByGuildIdAndUserId(guildId, userId).orElseThrow(() -> new MemberNotFoundException("User is not a member of this guild"));

//        if (memberFromGuildToDelete.isOwner()) {
//            guildRepository.delete(guildToDelete);
//            return;
//        }

        if(!memberFromGuildToDelete.isOwner())
        {
            throw new NotRightsException("You don't have enough rights");
        }


//        boolean isAdmin = memberFromGuildToDelete.getRoles().stream()
//                .flatMap(role -> role.getPermissions().stream())
//                .anyMatch(permission -> permission == Permission.ADMINISTRATOR);
//
//        if (!isAdmin) {
//            throw new NotRightsException("You don't have enough rights");
//        }

        guildRepository.delete(guildToDelete);
    }

    @Transactional
    public void joinMember(Long guildId, Long userId) {

        if(memberRepository.existsByGuildIdAndUserId(guildId, userId)) {
            throw new MemberAlreadyExistInGuildException("You are already member of this guild");
        }

        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild not found with id: " + guildId));

        GuildJoinStrategy strategy = joinStrategyFactory.getStrategy(guild.getType());

        strategy.validateJoin(guild, userId);

        saveNewMember(guild, userId);
    }

    @Transactional
    public void sendInvite(Long guildId, Long adminId, Long targetUserId) {

        if (memberRepository.existsByGuildIdAndUserId(guildId, targetUserId)) {
            throw new MemberAlreadyExistInGuildException("User is already in the guild");
        }


        if (inviteRepository.existsByGuildIdAndTargetUserIdAndStatus(guildId, targetUserId, InviteStatus.PENDING)) {
            throw new InviteAlreadyExistsException("Invite already sent to this user");
        }


        Member admin = memberRepository.findByGuildIdAndUserId(guildId, adminId)
                .orElseThrow(() -> new MemberNotFoundException("Admin not found"));

        if (!admin.hasPermission(Permission.CREATE_INSTANT_INVITE)) {
            throw new NotRightsException("You don't have rights to invite members");
        }


        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild not found"));


        Invite invite = Invite.builder()
                .guild(guild)
                .inviterId(adminId)
                .targetUserId(targetUserId)
                .status(InviteStatus.PENDING)
                .build();

        inviteRepository.save(invite);

        // ТУТ можна відправити повідомлення в Notification Service,
        // щоб юзер побачив "Вас запросили в гільдію Х"
    }


    @Transactional
    public void acceptInvite(Long inviteId, Long currentUserId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getTargetUserId().equals(currentUserId)) {
            throw new AccessDeniedException("This invite is not for you");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is already processed");
        }


        if (memberRepository.existsByGuildIdAndUserId(invite.getGuild().getId(), currentUserId)) {
            invite.setStatus(InviteStatus.ACCEPTED);
            inviteRepository.save(invite);
            return;
        }


        saveNewMember(invite.getGuild(), currentUserId);


        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
    }

    @Transactional
    public void declineInvite(Long inviteId, Long currentUserId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (!invite.getTargetUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Not your invite");
        }

        invite.setStatus(InviteStatus.DECLINED);
        inviteRepository.save(invite);
    }

    private void saveNewMember(Guild guild, Long userId) {
        Member newMember = Member.builder()
                .guild(guild)
                .userId(userId)
                .nickname(null)
                .roles(new HashSet<>())
                .build();

        Role defaultRole = roleRepository.findByNameAndGuildId("@everyone", guild.getId());
        if (defaultRole != null) {
            newMember.getRoles().add(defaultRole);
        }
        memberRepository.save(newMember);
    }

    @Transactional
    public List<InviteDto> getMyInvites(Long userId) {
        List<Invite> invites = inviteRepository.findAllByTargetUserIdAndStatus(userId, InviteStatus.PENDING);


        return invites.stream()
                .map(invite -> InviteDto.builder()
                        .id(invite.getId())
                        .status(invite.getStatus())
                        .inviterId(invite.getInviterId())

                        .guildId(invite.getGuild().getId())
                        .guildName(invite.getGuild().getName())
                        .guildIconUrl(invite.getGuild().getIconUrl())
                        .build())
                .toList();
    }

    public void deleteMember(Long guildId, Long memberId, Long userId) {
        Member memberToDelete =
                memberRepository.findByIdAndGuildId(memberId, guildId)
                        .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        Member admin = memberRepository.findByGuildIdAndUserId(guildId, userId)
                .orElseThrow(() -> new MemberNotFoundException("Admin not found"));

        if(!admin.hasPermission(Permission.KICK_MEMBERS))
        {
            throw new NotRightsException("You don't have enough rights");
        }

        if (memberToDelete.isOwner()) {
            throw new NotRightsException("You cannot remove guild owner");
        }


        memberRepository.delete(memberToDelete);

    }

    @Transactional
    public List<MemberDto> getAllMembers(Long guildId) {
        List<Member> members = memberRepository.findAllByGuildId(guildId);

        return members.stream()
                .map(this::toDto)
                .toList();
    }

    private MemberDto toDto(Member member) {
        MemberDto dto = new MemberDto();

        dto.setId(member.getId());
        dto.setUserId(member.getUserId());
        dto.setNickname(member.getNickname());
        dto.setJoinedAt(member.getJoinedAt());

        dto.setRoles(
                member.getRoles().stream()
                        .map(role -> {
                            RoleDto roleDto = new RoleDto();
                            roleDto.setId(role.getId());
                            roleDto.setName(role.getName());
                            roleDto.setColor(role.getColor());
                            roleDto.setPermissions(role.getPermissions());
                            return roleDto;
                        })
                        .collect(Collectors.toSet())
        );

        return dto;
    }

    @Transactional
    public void assignRoleToMember(
            Long guildId,
            ChangeRolesRequestDto dto,
            Long adminUserId
    ) {


        Member targetMember = memberRepository
                .findByIdAndGuildId(dto.getMemberId(), guildId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));


        Member admin = memberRepository
                .findByGuildIdAndUserId(guildId, adminUserId)
                .orElseThrow(() -> new MemberNotFoundException("Admin not found"));

        if (targetMember.isOwner()) {
            throw new NotRightsException("You cannot change roles of guild owner");
        }


        if (!admin.hasPermission(Permission.MANAGE_ROLES)) {
            throw new NotRightsException("You don't have permission to manage roles");
        }


        List<Role> roles = roleRepository.findAllByIdInAndGuildId(dto.getRoleIds(), guildId);

        if (roles.size() != dto.getRoleIds().size()) {
            throw new IllegalArgumentException("One or more roles are invalid");
        }


        boolean assigningAdminRole = roles.stream()
                .anyMatch(r -> r.getPermissions().contains(Permission.ADMINISTRATOR));

        if (assigningAdminRole && !admin.hasPermission(Permission.ADMINISTRATOR)) {
            throw new NotRightsException("You cannot assign administrator role");
        }


        targetMember.getRoles().clear();
        targetMember.getRoles().addAll(roles);

        memberRepository.save(targetMember);
    }


    @Transactional
    public RoleDto createRole(
            Long guildId,
            Long userId,
            CreateRoleRequestDto dto
    ) {

        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild not found"));

        Member creator = memberRepository
                .findByGuildIdAndUserId(guildId, userId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        checkCreateRolePermission(creator);

        validatePermissions(creator, dto.getPermissions());

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }


        if (roleRepository.existsByGuildIdAndNameIgnoreCase(guildId, dto.getName())) {
            throw new IllegalStateException("Role with this name already exists");
        }


        Role role = Role.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .permissions(dto.getPermissions())
                .guild(guild)
                .build();

        roleRepository.save(role);

        return roleMapper.toDto(role);
    }




    private void checkCreateRolePermission(Member member) {
        if (
                !member.hasPermission(Permission.ADMINISTRATOR) &&
                        !member.hasPermission(Permission.MANAGE_ROLES)
        ) {
            throw new NotRightsException("You cannot create roles");
        }
    }

    private void validatePermissions(Member creator, Set<Permission> requested) {

        if (creator.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        for (Permission p : requested) {
            if (!creator.hasPermission(p)) {
                throw new NotRightsException(
                        "You cannot assign permissions you don't have"
                );
            }
        }
    }


    public void leaveServer(Long guildId, Long userId) {
        Guild guild = guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException("Guild not found"));

        Member member = memberRepository.findByGuildIdAndUserId(guildId, userId).orElseThrow(() -> new MemberNotFoundException("Member not found"));

        if(member.isOwner()) {
            throw new NotRightsException("Owner cannot leave the guild. Transfer ownership or delete the guild.");
        }

        memberRepository.delete(member);

    }

    @Transactional
    public GuildDto transferOfOwnership(Long guildId, Long newOwnerId, Long oldOwnerId) {
        Guild guild = guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException("Guild not found"));
        Member newOwner = memberRepository.findById(newOwnerId).orElseThrow(() -> new MemberNotFoundException("Member not found"));
        Member oldOwner = memberRepository.findByGuildIdAndUserId(guildId, oldOwnerId).orElseThrow(() -> new MemberNotFoundException("Old owner not found"));

        if (!newOwner.getGuild().getId().equals(guildId)) {
            throw new NotRightsException("Member is not from this guild");
        }

        if (oldOwner.getUserId().equals(newOwner.getUserId())) {
            throw new NotRightsException("You already own this guild");
        }

        if (!oldOwner.isOwner()){
            throw new NotRightsException("You do not own this guild");
        }

        guild.setOwnerId(newOwner.getUserId());
        guildRepository.save(guild);

        return guildMapper.toDto(guild);

    }

    public List<GuildDto> getMyGuilds(Long userId) {
        log.info("Getting guilds for user: {}", userId);


        List<Guild> guilds = guildRepository.findAllByUserId(userId);


        return guildMapper.toDtos(guilds);
    }

    public List<String> getGuildIdsForUser(String userId) {
        return guildRepository.findGuildIdsByUserId(userId);
    }

//    public List<MemberSummaryDto> getMembersSummary(List<Long> memberIds) {
//        List<Member> members = memberRepository.findAllByIdIn(memberIds);
//
//        // Собираем ID тех, у кого нет никнейма
//        List<Long> userIdsToFetch = members.stream()
//                .filter(m -> m.getNickname() == null)
//                .map(Member::getUserId)
//                .distinct()
//                .toList();
//
//        // Делаем ОДИН запрос по gRPC
//        Map<Long, String> fetchedUsernames = userGrpcClient.getUsernames(userIdsToFetch);
//
//        return members.stream()
//                .map(member -> {
//                    String displayName = member.getNickname();
//
//                    // Если никнейма нет, берем из полученных по gRPC
//                    if (displayName == null) {
//                        displayName = fetchedUsernames.getOrDefault(member.getUserId(), "Unknown User");
//                    }
//
//                    Set<String> roleNames = member.getRoles().stream()
//                            .map(Role::getName) // Предполагаю, что у Role есть метод getName()
//                            .collect(Collectors.toSet());
//
//                    return MemberSummaryDto.builder()
//                            .id(member.getId())
//                            .userId(member.getUserId())
//                            .displayName(displayName)
//                            .roles(roleNames)
//                            .joinedAt(member.getJoinedAt())
//                            .build();
//                })
//                .toList();
//    }
}
