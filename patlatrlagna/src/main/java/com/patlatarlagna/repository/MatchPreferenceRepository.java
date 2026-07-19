package com.patlatarlagna.repository;

import com.patlatarlagna.entity.MatchPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MatchPreferenceRepository extends JpaRepository<MatchPreference, Long> {
    Optional<MatchPreference> findByUserId(Long userId);
}
