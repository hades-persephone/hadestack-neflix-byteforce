package io.watch.auth.service.impl;

import io.watch.auth.entity.RefreshToken;
import io.watch.auth.entity.User;
import io.watch.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken reToken = new RefreshToken();
        reToken.setToken(refreshToken);
        reToken.setUser(user);
        reToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(reToken);
    }



}
