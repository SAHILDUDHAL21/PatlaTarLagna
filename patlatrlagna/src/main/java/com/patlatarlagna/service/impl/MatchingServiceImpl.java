package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.InterestDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.*;
import com.patlatarlagna.enums.InterestStatus;
import com.patlatarlagna.enums.NotificationType;
import com.patlatarlagna.enums.ReportStatus;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.mapper.InterestMapper;
import com.patlatarlagna.mapper.ProfileMapper;
import com.patlatarlagna.repository.*;
import com.patlatarlagna.service.MatchingService;
import com.patlatarlagna.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchingServiceImpl implements MatchingService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MatchPreferenceRepository preferenceRepository;
    private final InterestRepository interestRepository;
    private final MatchRepository matchRepository;
    private final BlockRepository blockRepository;
    private final ReportRepository reportRepository;
    private final PhotoRepository photoRepository;
    private final NotificationService notificationService;
    private final InterestMapper interestMapper;
    private final ProfileMapper profileMapper;

    public MatchingServiceImpl(UserRepository userRepository, ProfileRepository profileRepository,
                               MatchPreferenceRepository preferenceRepository, InterestRepository interestRepository,
                               MatchRepository matchRepository, BlockRepository blockRepository,
                               ReportRepository reportRepository, PhotoRepository photoRepository,
                               NotificationService notificationService, InterestMapper interestMapper,
                               ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.preferenceRepository = preferenceRepository;
        this.interestRepository = interestRepository;
        this.matchRepository = matchRepository;
        this.blockRepository = blockRepository;
        this.reportRepository = reportRepository;
        this.photoRepository = photoRepository;
        this.notificationService = notificationService;
        this.interestMapper = interestMapper;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional
    public void sendInterest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new BadRequestException("Cannot express interest in your own profile");
        }

        if (blockRepository.existsByBlockerIdAndBlockedId(receiverId, senderId)) {
            throw new BadRequestException("You have been blocked by this user");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Optional<Interest> existing = interestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        if (existing.isPresent()) {
            throw new BadRequestException("Interest already sent to this user");
        }

        Interest interest = Interest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(InterestStatus.PENDING)
                .build();

        interestRepository.save(interest);

        // Notify receiver
        String senderName = profileRepository.findByUserId(senderId).map(Profile::getName).orElse("Someone");
        notificationService.createNotification(receiverId, senderName + " has expressed interest in your profile.", NotificationType.INTEREST);
    }

    @Override
    @Transactional
    public void acceptInterest(Long receiverId, Long interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));

        if (!interest.getReceiver().getId().equals(receiverId)) {
            throw new BadRequestException("Unauthorized action");
        }

        interest.setStatus(InterestStatus.ACCEPTED);
        interestRepository.save(interest);

        // Check if mutual match already exists
        Long u1 = interest.getSender().getId();
        Long u2 = interest.getReceiver().getId();

        if (matchRepository.findMatchBetween(u1, u2).isEmpty()) {
            double compatibility = calculateCompatibility(u1, u2);
            Match match = Match.builder()
                    .userOne(interest.getSender())
                    .userTwo(interest.getReceiver())
                    .compatibilityPercentage(compatibility)
                    .build();
            matchRepository.save(match);
        }

        // Notify sender
        String receiverName = profileRepository.findByUserId(receiverId).map(Profile::getName).orElse("Someone");
        notificationService.createNotification(interest.getSender().getId(), receiverName + " accepted your interest. You are now matched!", NotificationType.MATCH);
    }

    @Override
    @Transactional
    public void rejectInterest(Long receiverId, Long interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));

        if (!interest.getReceiver().getId().equals(receiverId)) {
            throw new BadRequestException("Unauthorized action");
        }

        interest.setStatus(InterestStatus.REJECTED);
        interestRepository.save(interest);
    }

    @Override
    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BadRequestException("Cannot block yourself");
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker not found"));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocked user not found"));

        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return; // already blocked
        }

        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        blockRepository.save(block);
    }

    @Override
    @Transactional
    public void reportUser(Long reporterId, Long reportedId, String reason, String details) {
        if (reporterId.equals(reportedId)) {
            throw new BadRequestException("Cannot report yourself");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));
        User reported = userRepository.findById(reportedId)
                .orElseThrow(() -> new ResourceNotFoundException("Reported user not found"));

        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .details(details)
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestDto> getSentInterests(Long userId) {
        return interestRepository.findBySenderId(userId).stream()
                .map(this::populateInterestDetails)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestDto> getReceivedInterests(Long userId) {
        return interestRepository.findByReceiverId(userId).stream()
                .map(this::populateInterestDetails)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileDto> getMutualMatches(Long userId) {
        return matchRepository.findAllMatchesForUser(userId).stream()
                .map(match -> {
                    User otherUser = match.getUserOne().getId().equals(userId) ? match.getUserTwo() : match.getUserOne();
                    return profileRepository.findByUserId(otherUser.getId()).orElse(null);
                })
                .filter(Objects::nonNull)
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCompatibility(Long userOneId, Long userTwoId) {
        Profile p1 = profileRepository.findByUserId(userOneId).orElse(null);
        Profile p2 = profileRepository.findByUserId(userTwoId).orElse(null);
        
        if (p1 == null || p2 == null) {
            return 75.0; // default baseline compatibility
        }

        MatchPreference pref1 = preferenceRepository.findByUserId(userOneId).orElse(null);
        MatchPreference pref2 = preferenceRepository.findByUserId(userTwoId).orElse(null);

        double score1 = calculatePreferenceScore(p2, pref1);
        double score2 = calculatePreferenceScore(p1, pref2);

        // Return the average score of both users matching each other's preferences
        return Math.round(((score1 + score2) / 2.0) * 10.0) / 10.0;
    }

    private double calculatePreferenceScore(Profile profile, MatchPreference preference) {
        if (preference == null) {
            return 75.0; // baseline if no preferences set
        }

        double score = 30.0; // base score

        // 1. Age Preference Match (up to 15 points)
        if (preference.getPreferredAgeMin() != null && preference.getPreferredAgeMax() != null) {
            if (profile.getAge() >= preference.getPreferredAgeMin() && profile.getAge() <= preference.getPreferredAgeMax()) {
                score += 15.0;
            }
        } else {
            score += 10.0;
        }

        // 2. Height Preference Match (up to 10 points)
        if (preference.getPreferredHeightMin() != null && preference.getPreferredHeightMax() != null) {
            if (profile.getHeight() >= preference.getPreferredHeightMin() && profile.getHeight() <= preference.getPreferredHeightMax()) {
                score += 10.0;
            }
        } else {
            score += 7.0;
        }

        // 3. Religion Match (up to 15 points)
        if (preference.getReligion() != null && !preference.getReligion().isBlank()) {
            if (profile.getReligion() != null && profile.getReligion().equalsIgnoreCase(preference.getReligion())) {
                score += 15.0;
            }
        } else {
            score += 10.0;
        }

        // 4. Caste Match (up to 10 points)
        if (preference.getCaste() != null && !preference.getCaste().isBlank()) {
            if (profile.getCaste() != null && profile.getCaste().equalsIgnoreCase(preference.getCaste())) {
                score += 10.0;
            }
        } else {
            score += 7.0;
        }

        // 5. Lifestyle Match (up to 10 points)
        if (preference.getLifestyle() != null) {
            if (profile.getLifestyle() == preference.getLifestyle()) {
                score += 10.0;
            }
        } else {
            score += 7.0;
        }

        // 6. Location Match (up to 10 points)
        if (preference.getCity() != null && !preference.getCity().isBlank()) {
            if (profile.getCity() != null && profile.getCity().equalsIgnoreCase(preference.getCity())) {
                score += 10.0;
            }
        } else if (preference.getState() != null && !preference.getState().isBlank()) {
            if (profile.getState() != null && profile.getState().equalsIgnoreCase(preference.getState())) {
                score += 7.0;
            }
        } else {
            score += 5.0;
        }

        return Math.min(100.0, score);
    }

    private InterestDto populateInterestDetails(Interest interest) {
        InterestDto dto = interestMapper.toDto(interest);
        
        // Fill sender details
        Profile senderProfile = profileRepository.findByUserId(interest.getSender().getId()).orElse(null);
        if (senderProfile != null) {
            dto.setSenderName(senderProfile.getName());
            senderProfile.getPhotos().stream()
                    .filter(Photo::isMain)
                    .findFirst()
                    .ifPresent(photo -> dto.setSenderPhoto(photo.getPhotoUrl()));
        }

        // Fill receiver details
        Profile receiverProfile = profileRepository.findByUserId(interest.getReceiver().getId()).orElse(null);
        if (receiverProfile != null) {
            dto.setReceiverName(receiverProfile.getName());
            receiverProfile.getPhotos().stream()
                    .filter(Photo::isMain)
                    .findFirst()
                    .ifPresent(photo -> dto.setReceiverPhoto(photo.getPhotoUrl()));
        }

        return dto;
    }
}
