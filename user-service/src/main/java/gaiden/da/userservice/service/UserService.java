package gaiden.da.userservice.service;

import gaiden.da.userservice.domain.User;
import gaiden.da.userservice.dto.UserCredentialDto;
import gaiden.da.userservice.dto.UserDto;
import gaiden.da.userservice.dto.UserRegisteredEvent;
import gaiden.da.userservice.exceptionHandler.exception.*;
import gaiden.da.userservice.mappers.userMapper.UserMapper;
import gaiden.da.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Transactional
    public void savePublicKey(Long userId, String publicKey) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setPublicKey(publicKey);
//        userRepository.save(user);
    }


    @Cacheable(value = "userCredentials", key = "#username")
    public UserCredentialDto findUserCredentialDtoByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found : " + username + ". From method findUserCredentialDtoByUsername"));
        return userMapper.toUserCredentialDto(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserDto getUser(Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundById("User With Id " + userId + " Not Found"));

            if (user.getOwnGuildIds() != null) {
                user.getOwnGuildIds().size();
            }

            return new UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getAvatarUrl(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getOwnGuildIds() == null
                            ? List.of()
                            : List.copyOf(user.getOwnGuildIds()),
                    user.getPublicKey()
            );

        }else {
            throw new UserIDWasNotProvided("User ID Not Provided");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("User with name " + username + " not found"));
    }

    @Cacheable(value = "usersByUsername", key = "#username")
    public UserDto findUserByUsername(String username) {
        return userMapper.toUserDto(userRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("User with name " + username + " not found")));
    }

    public UserDto createUser(String username, String email, String password) {
        if (username == null) {
            throw new UsernameNotSpecified("Username not specified");
        } else if (email == null) {
            throw new EmailNotSpecified("Email not specified");
        } else if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExist("User with email " + email + " already exist");
        } else if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExist("User with name " + username + " already exist");
        }else {
            User user = new User(username, email, password);
            User savedUser = userRepository.save(user);

            UserRegisteredEvent event = new UserRegisteredEvent(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getUsername()
            );

            kafkaTemplate.send("user-registered-topic", event);


            return userMapper.toUserDto(savedUser);
        }


    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "usersByUsername", allEntries = true),
            @CacheEvict(value = "usersByUsername", allEntries = true),
            @CacheEvict(value = "userCredentials", allEntries = true)
    })
    public void delete(Long userId, Long userIdFromToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundById("User Not Found"));
        if (!user.getId().equals(userIdFromToken)) {
            throw new AccessDeniedException("Ви не маєте права видаляти цей профіль!");
        }

        userRepository.deleteById(userId);
    }


    public List<UserDto> getUsers(List<Long> usersId) {
        if (usersId == null) throw new UserIDWasNotProvided("User ID Not Provided");

        return usersId.stream()
                .map(this::getUser)
                .toList();

    }


    @Caching(put = {
            @CachePut(value = "users", key = "#userDto.id")
    }, evict = {
            @CacheEvict(value = "usersByUsername", allEntries = true),
            @CacheEvict(value = "usersByEmail", allEntries = true),
            @CacheEvict(value = "userCredentials", allEntries = true)
    })
    public UserDto changeUserData(UserDto userDto, Long userIdFromToken) {
        User oldUser = userRepository.findById(userDto.getId()).orElseThrow(() -> new UserNotFoundById("User With Id " + userDto.getId() + " Not Found"));

        if (!oldUser.getId().equals(userIdFromToken)) {
            throw new AccessDeniedException("Ви не маєте права змінювати цей профіль!");
        }

        if(userDto.getId() == null) {
            throw new UserIDWasNotProvided("User ID Not Provided");
        }
        if (userDto.getUsername() == null) {
            throw new UsernameNotSpecified("Username not specified");
        }
        if (userDto.getEmail() == null) {
            throw new EmailNotSpecified("Email not specified");
        }
        if (!userRepository.existsById(userDto.getId())) {
            throw new UserNotFoundById("User With Id " + userDto.getId() + " Not Found");
        }
        if (userDto.getUsername() != null && !userDto.getUsername().equals(oldUser.getUsername())) {
            Optional<User> existingUserWithNewUsername = userRepository.findByUsername(userDto.getUsername());
            if (existingUserWithNewUsername.isPresent() && !existingUserWithNewUsername.get().getId().equals(oldUser.getId())) {
                throw new UserAlreadyExist("User with name " + userDto.getUsername() + " already exist");
            }
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(oldUser.getEmail())) {
            Optional<User> existingUserWithNewEmail = userRepository.findByEmail(userDto.getEmail());
            if(existingUserWithNewEmail.isPresent() && !existingUserWithNewEmail.get().getId().equals(oldUser.getId())) {
                throw new UserAlreadyExist("User with email " + userDto.getEmail() + " already exist");
            }
        }


        oldUser.setUsername(userDto.getUsername());
        oldUser.setEmail(userDto.getEmail());
        oldUser.setOwnGuildIds(userDto.getOwnGuildIds());
        User newUser = userRepository.save(oldUser);

        return userMapper.toUserDto(newUser);
    }

    @Cacheable(value = "usersByEmail", key = "#email")
    public UserDto findUserByEmail(String email) {
        return userMapper.toUserDto(userRepository.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found")));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();


        return ids.stream()
                .map(this::getUser)
                .toList();
    }



    @Caching(put = {
            @CachePut(value = "users", key = "#userId")
    })
    @Transactional
    public UserDto updateAvatar(Long userId, MultipartFile file, Long userIdFromToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundById("User Not Found"));

        if (!user.getId().equals(userIdFromToken)) {
            throw new AccessDeniedException("You cannot edit this profile!");
        }

        try {
            String avatarUrl = s3Service.uploadFile(file.getOriginalFilename(), file.getBytes(), file.getContentType());
            user.setAvatarUrl(avatarUrl);
            User savedUser = userRepository.save(user);
            return userMapper.toUserDto(savedUser);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file", e);
        }
    }
}
