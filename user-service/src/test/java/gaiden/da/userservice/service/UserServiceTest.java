package gaiden.da.userservice.service;

import gaiden.da.userservice.dto.UserCredentialDto;
import gaiden.da.userservice.dto.UserDto;
import gaiden.da.userservice.exceptionHandler.exception.AccessDeniedException;
import gaiden.da.userservice.exceptionHandler.exception.UserAlreadyExist;
import gaiden.da.userservice.exceptionHandler.exception.UserIDWasNotProvided;
import gaiden.da.userservice.exceptionHandler.exception.UserNotFoundById;
import gaiden.da.userservice.mappers.userMapper.UserMapper;
import gaiden.da.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import gaiden.da.userservice.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("findUserCredentialDtoByUsername: Успішно повертає DTO")
    void findUserCredentialDtoByUsername_Success() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("hashedPass");

        UserCredentialDto expectedDto = UserCredentialDto.builder()
                .id(1L)
                .username(username)
                .password("hashedPass")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toUserCredentialDto(user)).thenReturn(expectedDto);


        UserCredentialDto actualDto = userService.findUserCredentialDtoByUsername(username);

        assertNotNull(actualDto);
        assertEquals(expectedDto.getUsername(), actualDto.getUsername());
        verify(userRepository).findByUsername(username);
        verify(userMapper).toUserCredentialDto(user);
    }

    @Test
    @DisplayName("findUserCredentialDtoByUsername: Кидає помилку, якщо юзер не знайдений")
    void findUserCredentialDtoByUsername_NotFound() {
        // GIVEN
        String username = "unknownUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UsernameNotFoundException.class,
                () -> userService.findUserCredentialDtoByUsername(username));

        // Переконуємось, що маппер не викликався, бо впали раніше
        verify(userMapper, never()).toUserCredentialDto(any());
    }

    // --- Тести для getUser (по ID) ---

    @Test
    @DisplayName("getUser: Успішно повертає UserDto по ID")
    void getUser_Success() {
        // GIVEN
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        List<Long> guildIds = List.of(10L, 20L);

        User user = new User();
        user.setId(userId);
        user.setUsername("TestName");
        user.setEmail("test@mail.com");
        user.setAvatarUrl(null);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setOwnGuildIds(guildIds);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        UserDto result = userService.getUser(userId);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("TestName", result.getUsername());
        assertEquals(guildIds, result.getOwnGuildIds());

        // Маппер у цьому методі не використовується (ти створюєш new UserDto вручну),
        // тому його верифікувати не треба.
    }

    @Test
    @DisplayName("getUser: Кидає UserIDWasNotProvided, якщо ID == null")
    void getUser_NullId() {
        // WHEN & THEN
        assertThrows(UserIDWasNotProvided.class, () -> userService.getUser(null));

        // Перевіряємо, що в базу навіть не ходили
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getUser: Кидає UserNotFoundById, якщо юзера немає в базі")
    void getUser_UserNotFound() {
        // GIVEN
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UserNotFoundById.class, () -> userService.getUser(userId));
    }

    // --- Тести для loadUserByUsername (Spring Security) ---

    @Test
    @DisplayName("loadUserByUsername: Успішно повертає UserDetails")
    void loadUserByUsername_Success() {
        // GIVEN
        String username = "admin";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // WHEN
        UserDetails userDetails = userService.loadUserByUsername(username);

        // THEN
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    @DisplayName("loadUserByUsername: Кидає помилку, якщо юзер не знайдений")
    void loadUserByUsername_NotFound() {
        // GIVEN
        String username = "ghost";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));
    }

    // --- Тесты для createUser ---

    @Test
    @DisplayName("createUser: Успішне створення юзера")
    void createUser_Success() {
        // GIVEN
        String username = "newUser";
        String email = "new@mail.com";
        String password = "123";

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setUsername(username);
        savedUser.setEmail(email);

        UserDto expectedDto = new UserDto(10L, username, email, null, null, null, null, null);

        // Налаштування моків
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty()); // Email вільний
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty()); // Username вільний
        when(userRepository.save(any(User.class))).thenReturn(savedUser); // Save повертає юзера
        when(userMapper.toUserDto(savedUser)).thenReturn(expectedDto);

        // WHEN
        UserDto result = userService.createUser(username, email, password);

        // THEN
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: Помилка, якщо такий email вже існує")
    void createUser_EmailExists() {
        // GIVEN
        String email = "exist@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // WHEN & THEN
        assertThrows(UserAlreadyExist.class,
                () -> userService.createUser("user", email, "pass"));

        verify(userRepository, never()).save(any());
    }

    // --- Тесты для delete (перевірка прав) ---

    @Test
    @DisplayName("delete: Успішне видалення свого профілю")
    void delete_Success() {
        // GIVEN
        Long userId = 1L;
        Long currentUserId = 1L;
        String currentUsername = "owner";

        User user = new User();
        user.setId(userId);
        user.setUsername(currentUsername); // Ім'я співпадає з тим, хто робить запит

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        userService.delete(userId, currentUserId);

        // THEN
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("delete: AccessDenied, якщо намагаєшся видалити чужий профіль")
    void delete_AccessDenied() {
        // GIVEN
        Long userId = 1L;
        Long requesterId = 2L;

        User victim = new User();
        victim.setId(userId);
        victim.setUsername("victim"); // Інше ім'я

        when(userRepository.findById(userId)).thenReturn(Optional.of(victim));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class,
                () -> userService.delete(userId, requesterId));

        verify(userRepository, never()).deleteById(any());
    }

    // --- Тесты для changeUserData (складна логіка) ---

    @Test
    @DisplayName("changeUserData: Успішна зміна даних")
    void changeUserData_Success() {
        // GIVEN
        String currentUsername = "me";
        UserDto inputDto = new UserDto(1L, "newMe", "new@mail.com", null, null, null, null, null);

        User oldUser = new User();
        oldUser.setId(1L);
        oldUser.setUsername(currentUsername); // Це мій профіль
        oldUser.setEmail("old@mail.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("newMe");

        when(userRepository.findById(1L)).thenReturn(Optional.of(oldUser));
        when(userRepository.existsById(1L)).thenReturn(true);
        // Перевіряємо, що нові дані не зайняті іншими (повертаємо empty або того ж юзера)
        when(userRepository.findByUsername("newMe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserDto(updatedUser)).thenReturn(inputDto);

        // WHEN
        UserDto result = userService.changeUserData(inputDto, inputDto.getId());

        // THEN
        assertNotNull(result);
        assertEquals("newMe", result.getUsername());
    }

    @Test
    @DisplayName("changeUserData: Помилка, якщо змінюєш чужий профіль")
    void changeUserData_AccessDenied() {
        // GIVEN
        Long hackerId = 2L; // 🔥 ID того, хто атакує

        UserDto inputDto = new UserDto(1L, "hacked", "h@h.com", null, null, null, null, null);

        User victim = new User();
        victim.setId(1L); // Власник

        when(userRepository.findById(1L)).thenReturn(Optional.of(victim));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class,
                // 🔥 Передаємо hackerId, який НЕ дорівнює 1L
                () -> userService.changeUserData(inputDto, hackerId));

        verify(userRepository, never()).save(any());
    }


}
