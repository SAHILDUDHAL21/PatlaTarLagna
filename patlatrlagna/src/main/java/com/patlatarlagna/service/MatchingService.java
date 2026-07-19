package com.patlatarlagna.service;

import com.patlatarlagna.dto.InterestDto;
import com.patlatarlagna.dto.ProfileDto;
import java.util.List;

public interface MatchingService {
    void sendInterest(Long senderId, Long receiverId);
    void acceptInterest(Long receiverId, Long interestId);
    void rejectInterest(Long receiverId, Long interestId);
    void blockUser(Long blockerId, Long blockedId);
    void reportUser(Long reporterId, Long reportedId, String reason, String details);
    List<InterestDto> getSentInterests(Long userId);
    List<InterestDto> getReceivedInterests(Long userId);
    List<ProfileDto> getMutualMatches(Long userId);
    double calculateCompatibility(Long userOneId, Long userTwoId);
}
