package gaiden.da.authservice.service;

import gaiden.da.authservice.domain.RefreshToken;
import gaiden.da.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public RefreshToken createRefreshToken(Long userId, UserDetails userDetails) {
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(jwtService.generateRefreshToken(userDetails, userId))
                .sessionExpiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
