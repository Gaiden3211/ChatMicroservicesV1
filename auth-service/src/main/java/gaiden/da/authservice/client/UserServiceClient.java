//package gaiden.da.authservice.client;
//
//import gaiden.da.authservice.dto.RegisterUserRequestDto;
//import gaiden.da.authservice.dto.UserCredentialDto;
//import gaiden.da.authservice.dto.UserResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.*;
//
//@Component
//@FeignClient(name = "user-service", url = "${services.user.url}")
//public interface UserServiceClient {
//    @GetMapping("/api/v1/user/username/{username}")
//    UserResponse getUserByUsername(@PathVariable("username") String username);
//
//    @PostMapping("/api/v1/user/create")
//    UserResponse createUser(@RequestBody RegisterUserRequestDto createUserRequest);
//
//    @GetMapping("/api/v1/user/{userId}")
//    UserResponse getUserById(@PathVariable("userId") Long userId);
//
//    @GetMapping("/api/v1/user/internal/credentials/{username}")
//    UserCredentialDto getUserCredentialsByUsername(@PathVariable("username") String username);
//
//    @GetMapping("/api/v1/user/email/{email}")
//    UserResponse getUserByEmail(@PathVariable("email") String email);
//
//}
