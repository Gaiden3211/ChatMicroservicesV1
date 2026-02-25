//package gaiden.da.authservice.controller;
//
//import gaiden.da.authservice.dto.AuthResponse;
//
//import gaiden.da.authservice.dto.RefreshTokenRequest;
//import gaiden.da.authservice.dto.RequestLogin;
//import gaiden.da.authservice.dto.RequestRegister;
//import gaiden.da.authservice.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("api/v1/auth/")
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RequestRegister requestRegister) {
//        AuthResponse authResponse =  authService.register(requestRegister);
//        return ResponseEntity.ok(authResponse);
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody RequestLogin requestLogin) {
//        AuthResponse authResponse =  authService.login(requestLogin);
//        return ResponseEntity.ok(authResponse);
//    }
//
//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest refreshToken) {
//        AuthResponse authResponse = authService.refreshToken(refreshToken.getRefresh_token());
//        return ResponseEntity.ok(authResponse);
//    }
//
//
//}
