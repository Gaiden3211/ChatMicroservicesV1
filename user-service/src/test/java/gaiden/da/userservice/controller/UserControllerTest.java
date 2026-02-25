package gaiden.da.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gaiden.da.userservice.dto.RequestUserDto;
import gaiden.da.userservice.dto.UserCredentialDto;
import gaiden.da.userservice.dto.UserDto;
import gaiden.da.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)// Тестуємо тільки цей контролер
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // Імітує HTTP-запити (без реального сервера)

    @MockBean
    private UserService userService; // Мокаємо сервіс

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Autowired
    private ObjectMapper objectMapper; // Для перетворення об'єктів у JSON і назад

    // --- GET /internal/credentials/{username} ---
    @Test
    @DisplayName("getUserCredential: Повертає 200 та облікові дані")
    void getUserCredential_ShouldReturn200() throws Exception {
        UserCredentialDto credentialDto = UserCredentialDto.builder()
                .id(1L)
                .username("testUser")
                .password("hashedPass")
                .build();

        when(userService.findUserCredentialDtoByUsername("testUser")).thenReturn(credentialDto);

        mockMvc.perform(get("/api/v1/user/internal/credentials/testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.password").value("hashedPass"));
    }

    // --- GET /{userId} ---
    @Test
    @DisplayName("getUser: Повертає 200 та DTO юзера по ID")
    void getUser_ShouldReturnUser() throws Exception {
        UserDto userDto = new UserDto(1L, "user1", "u1@mail.com", null, null, null, null, null);

        when(userService.getUser(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    // --- POST /create ---
    @Test
    @DisplayName("createUser: Повертає 201 Created та Location Header")
    void createUser_ShouldReturn201() throws Exception {
        // Вхідний JSON
        RequestUserDto requestDto = new RequestUserDto();
        requestDto.setUsername("newUser");
        requestDto.setEmail("new@mail.com");
        requestDto.setPassword("12345");

        // Результат від сервісу
        UserDto createdUser = new UserDto(10L, "newUser", "new@mail.com", null, LocalDateTime.now(), LocalDateTime.now(), null, null);

        when(userService.createUser("newUser", "new@mail.com", "12345"))
                .thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))) // Перетворюємо об'єкт у JSON рядок
                .andExpect(status().isCreated()) // 201
                .andExpect(header().string("Location", "http://localhost/api/v1/user/create/10")) // Перевіряємо хедер Location
                .andExpect(jsonPath("$.username").value("newUser"));
    }

    // --- DELETE /{userId} ---
    @Test
    @DisplayName("deleteUser: Повертає 204 No Content і перевіряє хедер")
    void deleteUser_ShouldReturn204() throws Exception {
        Long userId = 1L;
        Long userIdFromToken = 1L;
        String usernameHeader = "owner";

        mockMvc.perform(delete("/api/v1/user/{userId}", userId)
                        .header("X-User-Id", userIdFromToken)) // Додаємо обов'язковий хедер
                .andExpect(status().isNoContent()); // 204

        verify(userService).delete(userId, userIdFromToken);
    }

    // --- PATCH / ---
    @Test
    @DisplayName("updateUser: Повертає 200 і оновленого юзера")
    void updateUser_ShouldReturn200() throws Exception {
        String usernameHeader = "me";
        Long userIdFromToken = 1L;
        UserDto inputDto = new UserDto(1L, "updatedMe", "upd@mail.com", null, null, null, null, null);

        when(userService.changeUserData(any(UserDto.class), eq(userIdFromToken)))
                .thenReturn(inputDto);

        mockMvc.perform(patch("/api/v1/user")
                        .header("X-User-Id", userIdFromToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedMe"));
    }

    // --- GET /list/{usersId} ---
    @Test
    @DisplayName("getUsers: Повертає список юзерів")
    void getUsers_ShouldReturnList() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        List<UserDto> dtos = List.of(
                new UserDto(1L, "u1", "m1", null, null, null, null, null),
                new UserDto(2L, "u2", "m2", null, null, null, null, null)
        );

        when(userService.getUsers(ids)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/user/list/1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Перевіряємо розмір масиву
                .andExpect(jsonPath("$[0].username").value("u1"));
    }
}