package gaiden.da.guildservice.service;


import gaiden.da.guildservice.domain.Guild;
import gaiden.da.guildservice.domain.Member;
import gaiden.da.guildservice.dto.GuildDto;
import gaiden.da.guildservice.exceptions.NotRightsException;
import gaiden.da.guildservice.mapper.GuildMapper;
import gaiden.da.guildservice.repository.interfaces.GuildRepository;
import gaiden.da.guildservice.repository.interfaces.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuildServiceTest {

    @Mock
    private GuildRepository guildRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GuildMapper guildMapper; // Мокаем маппер, чтобы не зависеть от его реализации

    @InjectMocks
    private GuildService guildService; // Сюда подставятся моки

    // --- Тесты для transferOfOwnership ---

    @Test
    void transferOfOwnership_Success() {
        // GIVEN (Дано)
        Long guildId = 100L;
        Long oldOwnerUserId = 1L;
        Long newOwnerMemberId = 50L;
        Long newOwnerUserId = 2L;

        // Гильдия
        Guild guild = new Guild();
        guild.setId(guildId);
        guild.setOwnerId(oldOwnerUserId);

        // Старый владелец (Member)
        Member oldOwner = new Member();
        oldOwner.setUserId(oldOwnerUserId);
        oldOwner.setGuild(guild);

        Member newOwner = new Member();
        newOwner.setId(newOwnerMemberId);
        newOwner.setUserId(newOwnerUserId); // Другой юзер
        newOwner.setGuild(guild);

        // Настраиваем поведение моков
        when(guildRepository.findById(guildId)).thenReturn(Optional.of(guild));
        when(memberRepository.findById(newOwnerMemberId)).thenReturn(Optional.of(newOwner));
        when(memberRepository.findByGuildIdAndUserId(guildId, oldOwnerUserId)).thenReturn(Optional.of(oldOwner));

        // Маппер просто вернет заглушку
        when(guildMapper.toDto(any(Guild.class))).thenReturn(new GuildDto());

        // WHEN (Когда)
        guildService.transferOfOwnership(guildId, newOwnerMemberId, oldOwnerUserId);

        // THEN (Тогда)
        // 1. Проверяем, что владелец в гильдии изменился на ID нового юзера
        assertEquals(newOwnerUserId, guild.getOwnerId());

        // 2. Проверяем, что вызвался save
        verify(guildRepository).save(guild);
    }

    @Test
    void transferOfOwnership_ShouldThrow_WhenUserIsNotOwner() {
        // GIVEN
        Long guildId = 100L;
        Long hackerUserId = 999L;
        Long realOwnerId = 1L;

        // 1. Створюємо гільдію
        Guild guild = new Guild();
        guild.setId(guildId);
        guild.setOwnerId(realOwnerId);

        // 2. Хакер (той, хто намагається вкрасти права)
        Member hackerMember = new Member();
        hackerMember.setUserId(hackerUserId);
        hackerMember.setGuild(guild);

        // 3. Новий власник (просто якийсь мембер)
        // ВАЖЛИВО: Йому треба встановити гільдію, щоб не було NPE!
        Member randomNewOwner = new Member();
        randomNewOwner.setId(50L);
        randomNewOwner.setGuild(guild); // <--- ОСЬ ЦЬОГО НЕ ВИСТАЧАЛО

        // Налаштування моків
        when(guildRepository.findById(guildId)).thenReturn(Optional.of(guild));

        // Повертаємо хакера (він не овнер)
        when(memberRepository.findByGuildIdAndUserId(guildId, hackerUserId)).thenReturn(Optional.of(hackerMember));

        // Повертаємо нормального нового власника (з гільдією)
        when(memberRepository.findById(any())).thenReturn(Optional.of(randomNewOwner));

        // WHEN & THEN
        assertThrows(NotRightsException.class, () ->
                guildService.transferOfOwnership(guildId, 50L, hackerUserId)
        );

        verify(guildRepository, never()).save(any());
    }

    @Test
    void transferOfOwnership_ShouldThrow_WhenNewOwnerIsSameAsOld() {
        // GIVEN
        Long guildId = 100L;
        Long ownerUserId = 1L;
        Long ownerMemberId = 10L;

        Guild guild = new Guild();
        guild.setId(guildId);
        guild.setOwnerId(ownerUserId);

        Member ownerMember = new Member();
        ownerMember.setId(ownerMemberId);
        ownerMember.setUserId(ownerUserId);
        ownerMember.setGuild(guild);

        when(guildRepository.findById(guildId)).thenReturn(Optional.of(guild));
        when(memberRepository.findById(ownerMemberId)).thenReturn(Optional.of(ownerMember));
        when(memberRepository.findByGuildIdAndUserId(guildId, ownerUserId)).thenReturn(Optional.of(ownerMember));

        // WHEN & THEN
        NotRightsException exception = assertThrows(NotRightsException.class, () ->
                guildService.transferOfOwnership(guildId, ownerMemberId, ownerUserId)
        );

        assertEquals("You already own this guild", exception.getMessage());
        verify(guildRepository, never()).save(any());
    }

    @Test
    void transferOfOwnership_ShouldThrow_WhenNewOwnerFromOtherGuild() {
        // GIVEN
        Long guildId = 100L;
        Long otherGuildId = 200L;

        Guild guild1 = new Guild();
        guild1.setId(guildId);
        Guild guild2 = new Guild();
        guild2.setId(otherGuildId);

        Member stranger = new Member();
        stranger.setGuild(guild2); // Чужая гильдия

        when(guildRepository.findById(guildId)).thenReturn(Optional.of(guild1));
        when(memberRepository.findById(any())).thenReturn(Optional.of(stranger));
        when(memberRepository.findByGuildIdAndUserId(any(), any())).thenReturn(Optional.of(new Member())); // Old owner mock

        // WHEN & THEN
        assertThrows(NotRightsException.class, () ->
                guildService.transferOfOwnership(guildId, 50L, 1L)
        );

    }
}
