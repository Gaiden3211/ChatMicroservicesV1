package gaiden.da.authservice.security;

import gaiden.da.authservice.service.JwtService;
import gaiden.da.authservice.service.RefreshTokenService;
import gaiden.da.user.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @GrpcClient("user-service")
    private UserServiceGrpcGrpc.UserServiceGrpcBlockingStub userStub;

    @Value("${FRONTEND_URL:https://localhost:8443}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String rawEmail = oAuth2User.getAttribute("email");
        String googleName = oAuth2User.getAttribute("name");

        // 1. Нормалізація Email
        if (rawEmail == null) {
            log.error("Google did not return an email!");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email is missing from OAuth2 provider");
            return;
        }
        String email = rawEmail.trim().toLowerCase();
        log.info("✅ Google login success for: {}", email);

        long userId = 0;
        String finalUsername = null;

        try {

            UserResponse existingUser = userStub.getUserByEmail(
                    GetUserByEmailRequest.newBuilder().setEmail(email).build()
            );
            userId = existingUser.getId();
            finalUsername = existingUser.getUsername();
            log.info("User found via gRPC. ID: {}, Username: {}", userId, finalUsername);

        } catch (StatusRuntimeException e) {

            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.info("User not found via Email. Starting registration...");

                try {
                    String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());


                    String baseName;


                    if (googleName != null && !googleName.isBlank()) {
                        baseName = googleName.trim();
                    } else {
                        baseName = email.split("@")[0];
                    }


                    String cleanName = baseName.replaceAll("[^\\p{L}0-9_\\.\\-\\s]", "");


                    if (cleanName.isBlank()) {
                        cleanName = email.split("@")[0].replaceAll("[^\\p{L}0-9_\\.\\-]", "");
                    }


                    if (cleanName.isBlank()) {
                        cleanName = "User";
                    }


                    String usernameToCreate = cleanName;

                    try {

                        userStub.getUserByUsername(
                                GetUserByUsernameRequest.newBuilder().setUsername(cleanName).build()
                        );

                        int randomTag = 1000 + new Random().nextInt(9000);
                        usernameToCreate = cleanName + randomTag;
                        log.info("Username '{}' is taken. Generated new: {}", cleanName, usernameToCreate);

                    } catch (StatusRuntimeException nameEx) {
                        if (nameEx.getStatus().getCode() == Status.Code.NOT_FOUND) {

                            log.info("Username '{}' is available.", cleanName);
                        } else {
                            throw nameEx;
                        }
                    }


                    CreateUserRequest createRequest = CreateUserRequest.newBuilder()
                            .setEmail(email)
                            .setUsername(usernameToCreate)
                            .setPassword(dummyPassword)
                            .build();

                    UserResponse newUser = userStub.createUser(createRequest);
                    userId = newUser.getId();
                    finalUsername = newUser.getUsername();
                    log.info("Created new user. ID: {}, Username: {}", userId, finalUsername);

                } catch (StatusRuntimeException createEx) {

                    log.warn("Create failed (Status: {}). Retrying fetch.", createEx.getStatus().getCode());

                    try {
                        UserResponse retryUser = userStub.getUserByEmail(
                                GetUserByEmailRequest.newBuilder().setEmail(email).build()
                        );
                        userId = retryUser.getId();
                        finalUsername = retryUser.getUsername();
                        log.info("Retry fetch successful. ID: {}", userId);

                    } catch (StatusRuntimeException retryEx) {
                        log.error("CRITICAL: User supposedly exists but cannot be fetched. Email: {}", email);
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Account state inconsistent.");
                        return;
                    }
                }
            } else {
                log.error("gRPC error calling user-service", e);
                throw e;
            }
        }


        if (userId != 0 && finalUsername != null) {
            UserDetails userDetails = User.builder()
                    .username(finalUsername)
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();

            String accessToken = jwtService.generateAccessToken(userDetails, userId);
            refreshTokenService.createRefreshToken(userId, userDetails);

            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/index.html")
                    .queryParam("token", accessToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to resolve user ID");
        }
    }
}