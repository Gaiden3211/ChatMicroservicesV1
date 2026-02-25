//package gaiden.da.authservice.service;
//
//import feign.FeignException;
//import gaiden.da.authservice.client.UserServiceClient;
//import gaiden.da.authservice.domain.RefreshToken;
//import gaiden.da.authservice.dto.*;
//import gaiden.da.authservice.exceptionHandler.exceptions.CustomFeignException;
//import gaiden.da.authservice.exceptionHandler.exceptions.RegistrationException;
//import gaiden.da.authservice.repository.RefreshTokenRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//import java.util.Collections;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtService jwtService;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final RefreshTokenService refreshTokenService;
//    private final UserServiceClient userClient;
//    private final PasswordEncoder passwordEncoder;
//
//    @Value("${application.security.jwt.refresh-token.expiration}")
//    private long refreshTokenExpiration;
//
//    @Transactional
//    public AuthResponse register(RequestRegister requestRegister) {
//        String hashedPassword = passwordEncoder.encode(requestRegister.getPassword());
//        RegisterUserRequestDto createUserRequest = new RegisterUserRequestDto();
//        createUserRequest.setUsername(requestRegister.getUsername());
//        createUserRequest.setEmail(requestRegister.getEmail());
//        createUserRequest.setPassword(hashedPassword);
//
//        UserResponse createdUser;
//
//
//        try {
//            createdUser = userClient.createUser(createUserRequest);
//
//            if (createdUser == null || createdUser.id() == null) {
//                throw new RuntimeException("User Service returned invalid response during user creation.");
//            }
//        }
//        catch (CustomFeignException.UserAlreadyExists e) {
//
//            throw new RegistrationException("User already exists: " + requestRegister.getUsername() + " or " + requestRegister.getEmail());
//        }
//        catch (CustomFeignException.BadRequest e) {
//            throw new RegistrationException("Invalid registration data: " + e.getMessage());
//        }
//        catch (Exception e) {
//            throw new RuntimeException("Failed to create user in User Service: " + e.getMessage(), e);
//        }
//
//        UserDetails userDetails = User.builder()
//                .username(createdUser.username())
//                .password(hashedPassword)
//                .authorities(Collections.emptyList())
//                .build();
//
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(createdUser.id(), userDetails); // 👈 тепер ок
//
//        return AuthResponse.builder()
//                .access_token(accessToken)
//                .refresh_token(refreshToken.getToken())
//                .build();
//    }
//
//    public AuthResponse login(RequestLogin requestLogin) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        requestLogin.username(),
//                        requestLogin.password()
//                )
//        );
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//        UserResponse userResponse;
//        try {
//            userResponse = userClient.getUserByUsername(userDetails.getUsername());
//            if (userResponse == null || userResponse.id() == null) {
//                throw new RuntimeException("Could not retrieve user ID after login.");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to retrieve user details from User Service after login: " + e.getMessage(), e);
//        }
//
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userResponse.id(), userDetails); // 👈 виклик іншого біна
//
//        return AuthResponse.builder()
//                .access_token(accessToken)
//                .refresh_token(refreshToken.getToken())
//                .build();
//    }
//
//    @Transactional
//    public AuthResponse refreshToken(String requestRefreshToken) {
//        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
//                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
//
//        if (oldRefreshToken.getSessionExpiresAt().isBefore(Instant.now())) {
//            refreshTokenService.delete(oldRefreshToken);
//            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
//        }
//
//        UserResponse userResponse;
//        try {
//            userResponse = userClient.getUserById(oldRefreshToken.getUserId());
//            if (userResponse == null || userResponse.id() == null) {
//                throw new RuntimeException("Could not retrieve user ID after refresh token.");
//            }
//        } catch (Exception e) {
//            refreshTokenService.delete(oldRefreshToken);
//            throw new RuntimeException("Failed to retrieve user details for refresh token: " + e.getMessage(), e);
//        }
//
//        UserDetails userDetails = User.builder()
//                .username(userResponse.username())
//                .password("")
//                .authorities(Collections.emptyList())
//                .build();
//
//        String accessToken = jwtService.generateAccessToken(userDetails);
//        refreshTokenService.delete(oldRefreshToken); // 👈 тепер транзакційно
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(oldRefreshToken.getUserId(), userDetails);
//
//        return AuthResponse.builder()
//                .access_token(accessToken)
//                .refresh_token(refreshToken.getToken())
//                .build();
//    }
//}
