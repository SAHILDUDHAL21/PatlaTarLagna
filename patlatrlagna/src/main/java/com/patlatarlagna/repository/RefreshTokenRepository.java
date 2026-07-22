package com.patlatarlagna.repository;

import com.patlatarlagna.entity.RefreshToken;
import com.patlatarlagna.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    // Find refresh token by user (if any)
    Optional<RefreshToken> findByUser(User user);
    
    @Modifying
    int deleteByUser(User user);
}
