package gaiden.da.userservice.controller;

import gaiden.da.userservice.dto.RequestUserDto;
import gaiden.da.userservice.dto.UserCredentialDto;
import gaiden.da.userservice.dto.UserDto;
import gaiden.da.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User control")
public class UserController {

    private final UserService userService;




    @PostMapping("/{userId}/public-key")
    public ResponseEntity<Void> uploadPublicKey(@PathVariable Long userId, @RequestBody String publicKey, @RequestHeader("X-User-Id") Long userIdFromToken) {

        if (!userId.equals(userIdFromToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.savePublicKey(userId, publicKey);
        return ResponseEntity.ok().build();
    }



    @GetMapping("/internal/credentials/{username}")
    public ResponseEntity<UserCredentialDto> getUserCredential(@PathVariable String username) {
        UserCredentialDto userCredentialDto = userService.findUserCredentialDtoByUsername(username);
        return ResponseEntity.ok(userCredentialDto);
    }



    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        UserDto userDto = userService.getUser(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        UserDto userDto = userService.findUserByUsername(username);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto userDto = userService.findUserByEmail(email);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody RequestUserDto requestUserDto)  {
        UserDto userDto = userService.createUser(requestUserDto.getUsername(), requestUserDto.getEmail(), requestUserDto.getPassword());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, @RequestHeader("X-User-Id") Long userIdFromToken ) {
        userService.delete(userId, userIdFromToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam("ids") List<Long> userIds) {
        List<UserDto> usersDto = userService.getUsers(userIds);
        return ResponseEntity.ok(usersDto);
    }

    @Operation(summary = "Update user profile text data")
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto, @RequestHeader("X-User-Id") Long userIdFromToken ) {
        userDto.setId(userId);
        UserDto changedUserDto = userService.changeUserData(userDto, userIdFromToken);
        return ResponseEntity.ok(changedUserDto);
    }

    @Operation(summary = "Upload user avatar")
    @PostMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> uploadAvatar(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userIdFromToken
    ) {
        UserDto updatedUser = userService.updateAvatar(userId, file, userIdFromToken);
        return ResponseEntity.ok(updatedUser);
    }
}
