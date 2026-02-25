package gaiden.da.authservice.service;

import gaiden.da.authservice.domain.RefreshToken;
import gaiden.da.authservice.dto.AuthResponse;
import gaiden.da.authservice.dto.RequestLogin;
import gaiden.da.authservice.dto.RequestRegister;
import gaiden.da.authservice.exceptionHandler.exceptions.RegistrationException;
import gaiden.da.authservice.repository.RefreshTokenRepository;
import gaiden.da.user.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceGrpc {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;


    @GrpcClient("user-service")
    private UserServiceGrpcGrpc.UserServiceGrpcBlockingStub userStub;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthResponse register(RequestRegister requestRegister){

        try {
            GetUserByUsernameRequest req = GetUserByUsernameRequest.newBuilder()
                    .setUsername(requestRegister.getUsername())
                    .build();
            userStub.getUserByUsername(req);
            throw new RegistrationException("Username already exists: '" + requestRegister.getUsername() + "'");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() != Status.Code.NOT_FOUND) {
                throw e;
            }

        }


        try {
            GetUserByEmailRequest reqE = GetUserByEmailRequest.newBuilder()
                    .setEmail(requestRegister.getEmail())
                    .build();
            userStub.getUserByEmail(reqE);
            throw new RegistrationException("Email already exists: '" + requestRegister.getEmail() + "'");
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() != Status.Code.NOT_FOUND) {
                throw e;
            }
        }


        String hashedPassword = passwordEncoder.encode(requestRegister.getPassword());


        CreateUserRequest createUserRequest = CreateUserRequest.newBuilder()
                .setUsername(requestRegister.getUsername())
                .setEmail(requestRegister.getEmail())
                .setPassword(hashedPassword)
                .build();


        gaiden.da.user.UserResponse createdUser;
        try {
            createdUser = userStub.createUser(createUserRequest);
            if (createdUser == null || createdUser.getId() == 0) {
                throw new RegistrationException("User Service returned invalid response.");
            }
        } catch (Exception e) {
            throw new RegistrationException("Failed to create user: " + e.getMessage());
        }

        long userId = createdUser.getId();


        UserDetails userDetails = User.builder()
                .username(createdUser.getUsername())
                .password(hashedPassword)
                .authorities(Collections.emptyList())
                .build();


        String accessToken = jwtService.generateAccessToken(userDetails, userId);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId, userDetails);

        return AuthResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken.getToken())
                .build();
    }

    public AuthResponse login(RequestLogin requestLogin){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestLogin.username(),
                        requestLogin.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();


        gaiden.da.user.UserResponse userResponse;
        try {
            userResponse = userStub.getUserByUsername(GetUserByUsernameRequest.newBuilder()
                    .setUsername(userDetails.getUsername())
                    .build());
            if (userResponse == null || userResponse.getId() == 0) {
                throw new RegistrationException("Could not retrieve user ID after login.");
            }
        } catch (Exception e) {
            throw new RegistrationException("Failed to retrieve user details: " + e.getMessage());
        }


        String accessToken = jwtService.generateAccessToken(userDetails, userResponse.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userResponse.getId(), userDetails);

        return AuthResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken.getToken())
                .build();
    }


    @Transactional
    public AuthResponse refreshToken(String requestRefreshToken) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RegistrationException("Refresh token not found"));

        if (oldRefreshToken.getSessionExpiresAt().isBefore(Instant.now())) {
            refreshTokenService.delete(oldRefreshToken);
            throw new RegistrationException("Refresh token was expired.");
        }


        gaiden.da.user.UserResponse userResponse;
        try {
            userResponse = userStub.getUserById(GetUserByIdRequest.newBuilder()
                    .setUserId(oldRefreshToken.getUserId())
                    .build());
            if (userResponse == null) {
                throw new RegistrationException("User associated with token not found.");
            }
        } catch (Exception e) {
            refreshTokenService.delete(oldRefreshToken);
            throw new RegistrationException("Failed to retrieve user details: " + e.getMessage());
        }

        UserDetails userDetails = User.builder()
                .username(userResponse.getUsername())
                .password("")
                .authorities(Collections.emptyList())
                .build();


        String accessToken = jwtService.generateAccessToken(userDetails, userResponse.getId());
        refreshTokenService.delete(oldRefreshToken);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(oldRefreshToken.getUserId(), userDetails);

        return AuthResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken.getToken())
                .build();
    }




}
