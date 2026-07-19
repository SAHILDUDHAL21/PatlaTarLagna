package com.patlatarlagna.repository;

import com.patlatarlagna.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    
    @Query("SELECT m FROM Match m WHERE m.userOne.id = :userId OR m.userTwo.id = :userId")
    List<Match> findAllMatchesForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM Match m WHERE (m.userOne.id = :u1 AND m.userTwo.id = :u2) OR (m.userOne.id = :u2 AND m.userTwo.id = :u1)")
    Optional<Match> findMatchBetween(@Param("u1") Long u1, @Param("u2") Long u2);
}
