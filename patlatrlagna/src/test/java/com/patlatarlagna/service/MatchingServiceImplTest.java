package com.patlatarlagna.service;

import com.patlatarlagna.dto.InterestDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.*;
import com.patlatarlagna.enums.*;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.mapper.InterestMapper;
import com.patlatarlagna.mapper.ProfileMapper;
import com.patlatarlagna.repository.*;
import com.patlatarlagna.service.impl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MatchingServiceImpl — interest lifecycle, blocking,
 * reporting, compatibility scoring, and mutual matching logic.
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private MatchPreferenceRepository preferenceRepository;
    @Mock private InterestRepository interestRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private BlockRepository blockRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private PhotoRepository photoRepository;
    @Mock private NotificationService notificationService;
    @Mock private InterestMapper interestMapper;
    @Mock private ProfileMapper profileMapper;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private User userOne;
    private User userTwo;
    private Profile profileOne;
    private Profile profileTwo;

    @BeforeEach
    void setUp() {
        userOne = User.builder().id(1L).email("user1@example.com").enabled(true).build();
        userTwo = User.builder().id(2L).email("user2@example.com").enabled(true).build();

        profileOne = Profile.builder()
                .id(1L).user(userOne).name("User One").age(25)
                .gender(Gender.MALE).religion("Hindu").caste("Brahmin")
                .motherTongue("Hindi").height(175.0).weight(70.0)
                .education("B.Tech").occupation("Engineer")
                .annualIncome(1200000.0).maritalStatus(MaritalStatus.NEVER_MARRIED)
                .city("Pune").state("Maharashtra").country("India")
                .lifestyle(Lifestyle.VEGETARIAN).build();

        profileTwo = Profile.builder()
                .id(2L).user(userTwo).name("User Two").age(24)
                .gender(Gender.FEMALE).religion("Hindu").caste("Brahmin")
                .motherTongue("Hindi").height(160.0).weight(55.0)
                .education("M.B.B.S").occupation("Doctor")
                .annualIncome(1800000.0).maritalStatus(MaritalStatus.NEVER_MARRIED)
                .city("Pune").state("Maharashtra").country("India")
                .lifestyle(Lifestyle.VEGETARIAN).build();
    }

    // ========================================================================
    // SEND INTEREST
    // ========================================================================
    @Nested
    @DisplayName("sendInterest()")
    class SendInterestTests {

        @Test
        @DisplayName("Should send interest successfully")
        void sendInterest_success() {
            when(blockRepository.existsByBlockerIdAndBlockedId(2L, 1L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
            when(userRepository.findById(2L)).thenReturn(Optional.of(userTwo));
            when(interestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.empty());
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profileOne));

            matchingService.sendInterest(1L, 2L);

            verify(interestRepository).save(argThat(interest -> {
                assertThat(interest.getSender()).isEqualTo(userOne);
                assertThat(interest.getReceiver()).isEqualTo(userTwo);
                assertThat(interest.getStatus()).isEqualTo(InterestStatus.PENDING);
                return true;
            }));
            verify(notificationService).createNotification(eq(2L), contains("User One"), eq(NotificationType.INTEREST));
        }

        @Test
        @DisplayName("Should throw when sending interest to self")
        void sendInterest_self() {
            assertThatThrownBy(() -> matchingService.sendInterest(1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("own profile");
        }

        @Test
        @DisplayName("Should throw when blocked by receiver")
        void sendInterest_blocked() {
            when(blockRepository.existsByBlockerIdAndBlockedId(2L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> matchingService.sendInterest(1L, 2L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("blocked");
        }

        @Test
        @DisplayName("Should throw when interest already sent")
        void sendInterest_duplicate() {
            when(blockRepository.existsByBlockerIdAndBlockedId(2L, 1L)).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
            when(userRepository.findById(2L)).thenReturn(Optional.of(userTwo));

            Interest existing = Interest.builder().id(99L).sender(userOne).receiver(userTwo)
                    .status(InterestStatus.PENDING).build();
            when(interestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> matchingService.sendInterest(1L, 2L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already sent");
        }
    }

    // ========================================================================
    // ACCEPT / REJECT INTEREST
    // ========================================================================
    @Nested
    @DisplayName("acceptInterest() / rejectInterest()")
    class InterestResponseTests {

        private Interest pendingInterest;

        @BeforeEach
        void setUp() {
            pendingInterest = Interest.builder()
                    .id(10L)
                    .sender(userOne)
                    .receiver(userTwo)
                    .status(InterestStatus.PENDING)
                    .build();
        }

        @Test
        @DisplayName("Should accept interest and create match")
        void acceptInterest_success() {
            when(interestRepository.findById(10L)).thenReturn(Optional.of(pendingInterest));
            when(matchRepository.findMatchBetween(1L, 2L)).thenReturn(Optional.empty());
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profileOne));
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profileTwo));
            when(preferenceRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

            matchingService.acceptInterest(2L, 10L);

            assertThat(pendingInterest.getStatus()).isEqualTo(InterestStatus.ACCEPTED);
            verify(interestRepository).save(pendingInterest);
            verify(matchRepository).save(argThat(match -> {
                assertThat(match.getUserOne()).isEqualTo(userOne);
                assertThat(match.getUserTwo()).isEqualTo(userTwo);
                assertThat(match.getCompatibilityPercentage()).isGreaterThan(0);
                return true;
            }));
            verify(notificationService).createNotification(eq(1L), contains("accepted"), eq(NotificationType.MATCH));
        }

        @Test
        @DisplayName("Should throw when accepting interest for wrong user")
        void acceptInterest_unauthorized() {
            when(interestRepository.findById(10L)).thenReturn(Optional.of(pendingInterest));

            // User 1 tries to accept an interest where user 2 is the receiver
            assertThatThrownBy(() -> matchingService.acceptInterest(1L, 10L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Unauthorized");
        }

        @Test
        @DisplayName("Should reject interest successfully")
        void rejectInterest_success() {
            when(interestRepository.findById(10L)).thenReturn(Optional.of(pendingInterest));

            matchingService.rejectInterest(2L, 10L);

            assertThat(pendingInterest.getStatus()).isEqualTo(InterestStatus.REJECTED);
            verify(interestRepository).save(pendingInterest);
        }

        @Test
        @DisplayName("Should throw when interest not found")
        void rejectInterest_notFound() {
            when(interestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.rejectInterest(2L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========================================================================
    // BLOCK USER
    // ========================================================================
    @Nested
    @DisplayName("blockUser()")
    class BlockUserTests {

        @Test
        @DisplayName("Should block user successfully")
        void blockUser_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
            when(userRepository.findById(2L)).thenReturn(Optional.of(userTwo));
            when(blockRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(false);

            matchingService.blockUser(1L, 2L);

            verify(blockRepository).save(argThat(block -> {
                assertThat(block.getBlocker()).isEqualTo(userOne);
                assertThat(block.getBlocked()).isEqualTo(userTwo);
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw when blocking self")
        void blockUser_self() {
            assertThatThrownBy(() -> matchingService.blockUser(1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("yourself");
        }

        @Test
        @DisplayName("Should do nothing when already blocked")
        void blockUser_alreadyBlocked() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
            when(userRepository.findById(2L)).thenReturn(Optional.of(userTwo));
            when(blockRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(true);

            matchingService.blockUser(1L, 2L);

            verify(blockRepository, never()).save(any());
        }
    }

    // ========================================================================
    // REPORT USER
    // ========================================================================
    @Nested
    @DisplayName("reportUser()")
    class ReportUserTests {

        @Test
        @DisplayName("Should report user successfully")
        void reportUser_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
            when(userRepository.findById(2L)).thenReturn(Optional.of(userTwo));

            matchingService.reportUser(1L, 2L, "Fake profile", "Details here");

            verify(reportRepository).save(argThat(report -> {
                assertThat(report.getReporter()).isEqualTo(userOne);
                assertThat(report.getReported()).isEqualTo(userTwo);
                assertThat(report.getReason()).isEqualTo("Fake profile");
                assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw when reporting self")
        void reportUser_self() {
            assertThatThrownBy(() -> matchingService.reportUser(1L, 1L, "test", "test"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("yourself");
        }
    }

    // ========================================================================
    // COMPATIBILITY SCORE
    // ========================================================================
    @Nested
    @DisplayName("calculateCompatibility()")
    class CompatibilityTests {

        @Test
        @DisplayName("Should return baseline 75.0 when profiles are missing")
        void compatibility_noProfiles() {
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.empty());

            double score = matchingService.calculateCompatibility(1L, 2L);
            assertThat(score).isEqualTo(75.0);
        }

        @Test
        @DisplayName("Should return baseline when no preferences are set")
        void compatibility_noPreferences() {
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profileOne));
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profileTwo));
            when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(preferenceRepository.findByUserId(2L)).thenReturn(Optional.empty());

            double score = matchingService.calculateCompatibility(1L, 2L);
            // Both profiles use baseline 75.0 when no preferences → average = 75.0
            assertThat(score).isEqualTo(75.0);
        }

        @Test
        @DisplayName("Should return high compatibility for perfectly matching profiles")
        void compatibility_perfectMatch() {
            MatchPreference pref1 = MatchPreference.builder()
                    .user(userOne)
                    .preferredAgeMin(22).preferredAgeMax(28)
                    .preferredHeightMin(155.0).preferredHeightMax(170.0)
                    .religion("Hindu").caste("Brahmin")
                    .lifestyle(Lifestyle.VEGETARIAN)
                    .city("Pune").state("Maharashtra")
                    .build();

            MatchPreference pref2 = MatchPreference.builder()
                    .user(userTwo)
                    .preferredAgeMin(23).preferredAgeMax(30)
                    .preferredHeightMin(170.0).preferredHeightMax(185.0)
                    .religion("Hindu").caste("Brahmin")
                    .lifestyle(Lifestyle.VEGETARIAN)
                    .city("Pune").state("Maharashtra")
                    .build();

            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profileOne));
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profileTwo));
            when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(pref1));
            when(preferenceRepository.findByUserId(2L)).thenReturn(Optional.of(pref2));

            double score = matchingService.calculateCompatibility(1L, 2L);

            // Both profiles match all preferences perfectly → 100 each → avg = 100
            assertThat(score).isGreaterThanOrEqualTo(90.0);
        }

        @Test
        @DisplayName("Should return lower compatibility when preferences do not match")
        void compatibility_lowMatch() {
            MatchPreference pref1 = MatchPreference.builder()
                    .user(userOne)
                    .preferredAgeMin(30).preferredAgeMax(35) // User Two is 24, out of range
                    .preferredHeightMin(170.0).preferredHeightMax(185.0) // 160 is out of range
                    .religion("Christian") // mismatch
                    .caste("Kayastha") // mismatch
                    .lifestyle(Lifestyle.NON_VEGETARIAN) // mismatch
                    .city("Delhi") // mismatch
                    .build();

            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profileOne));
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profileTwo));
            when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(pref1));
            when(preferenceRepository.findByUserId(2L)).thenReturn(Optional.empty());

            double score = matchingService.calculateCompatibility(1L, 2L);

            // User one's prefs don't match user two at all → only base score (30)
            // User two has no prefs → baseline 75
            // Average = (30 + 75) / 2 = 52.5
            assertThat(score).isLessThan(60.0);
        }
    }

    // ========================================================================
    // QUERY: SENT / RECEIVED INTERESTS, MUTUAL MATCHES
    // ========================================================================
    @Nested
    @DisplayName("getSentInterests() / getReceivedInterests() / getMutualMatches()")
    class QueryTests {

        @Test
        @DisplayName("Should return sent interests list")
        void getSentInterests() {
            Interest interest = Interest.builder()
                    .id(1L).sender(userOne).receiver(userTwo)
                    .status(InterestStatus.PENDING).build();

            InterestDto dto = new InterestDto();
            dto.setId(1L);
            dto.setStatus(InterestStatus.PENDING);

            when(interestRepository.findBySenderId(1L)).thenReturn(List.of(interest));
            when(interestMapper.toDto(interest)).thenReturn(dto);
            when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profileOne));
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profileTwo));

            List<InterestDto> result = matchingService.getSentInterests(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSenderName()).isEqualTo("User One");
            assertThat(result.get(0).getReceiverName()).isEqualTo("User Two");
        }

        @Test
        @DisplayName("Should return mutual matches")
        void getMutualMatches() {
            Match match = Match.builder()
                    .id(1L).userOne(userOne).userTwo(userTwo)
                    .compatibilityPercentage(85.0).build();

            ProfileDto profileDto = new ProfileDto();
            profileDto.setName("User Two");

            when(matchRepository.findAllMatchesForUser(1L)).thenReturn(List.of(match));
            when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profileTwo));
            when(profileMapper.toDto(profileTwo)).thenReturn(profileDto);

            List<ProfileDto> result = matchingService.getMutualMatches(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("User Two");
        }
    }
}
