package gaiden.da.authservice.controller;

import gaiden.da.authservice.dto.AuthResponse;
import gaiden.da.authservice.dto.RefreshTokenRequest;
import gaiden.da.authservice.dto.RequestLogin;
import gaiden.da.authservice.dto.RequestRegister;
import gaiden.da.authservice.service.AuthServiceGrpc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth/grpc/")
@Tag(name = "Auth", description = "Auth control")
public class AuthControllerGrpc {

    private final AuthServiceGrpc authService;

    @PostMapping("register")
    @Operation(summary = "Register")
    public ResponseEntity<AuthResponse> register(@RequestBody RequestRegister requestRegister) {
        AuthResponse authResponse =  authService.register(requestRegister);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody RequestLogin requestLogin) {
        AuthResponse authResponse =  authService.login(requestLogin);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Refresh")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest refreshToken) {
        AuthResponse authResponse = authService.refreshToken(refreshToken.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }
}
