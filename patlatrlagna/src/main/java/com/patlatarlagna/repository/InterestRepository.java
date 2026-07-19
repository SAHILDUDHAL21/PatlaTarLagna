package com.patlatarlagna.repository;

import com.patlatarlagna.entity.Interest;
import com.patlatarlagna.enums.InterestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    List<Interest> findBySenderId(Long senderId);
    List<Interest> findByReceiverId(Long receiverId);
    Optional<Interest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    
    @Query("SELECT i FROM Interest i WHERE (i.sender.id = :u1 AND i.receiver.id = :u2) OR (i.sender.id = :u2 AND i.receiver.id = :u1)")
    Optional<Interest> findInterestBetween(@Param("u1") Long u1, @Param("u2") Long u2);
}
