package com.patlatarlagna.service.impl;

import com.patlatarlagna.dto.DashboardStatsDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.Profile;
import com.patlatarlagna.entity.Report;
import com.patlatarlagna.entity.User;
import com.patlatarlagna.enums.NotificationType;
import com.patlatarlagna.enums.ReportStatus;
import com.patlatarlagna.enums.RoleType;
import com.patlatarlagna.exception.BadRequestException;
import com.patlatarlagna.exception.ResourceNotFoundException;
import com.patlatarlagna.mapper.ProfileMapper;
import com.patlatarlagna.repository.*;
import com.patlatarlagna.service.AdminService;
import com.patlatarlagna.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;
    private final ProfileMapper profileMapper;

    public AdminServiceImpl(UserRepository userRepository, ProfileRepository profileRepository,
                            ReportRepository reportRepository,
                            NotificationService notificationService, ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.reportRepository = reportRepository;
        this.notificationService = notificationService;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long pendingVerifications = profileRepository.findAll().stream()
                .filter(p -> !p.isVerified())
                .count();
        long totalReports = reportRepository.count();

        // Setup mock distributions for the charts
        Map<String, Long> userRegistrationTrend = new HashMap<>();
        userRegistrationTrend.put("April 2026", 12L);
        userRegistrationTrend.put("May 2026", 24L);
        userRegistrationTrend.put("June 2026", 45L);
        userRegistrationTrend.put("July 2026", totalUsers);

        Map<String, Long> distributionByGender = new HashMap<>();
        long males = profileRepository.findAll().stream().filter(p -> p.getGender() == com.patlatarlagna.enums.Gender.MALE).count();
        long females = profileRepository.findAll().stream().filter(p -> p.getGender() == com.patlatarlagna.enums.Gender.FEMALE).count();
        distributionByGender.put("Male", males);
        distributionByGender.put("Female", females);

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .pendingVerifications(pendingVerifications)
                .totalReports(totalReports)
                .userRegistrationTrend(userRegistrationTrend)
                .distributionByGender(distributionByGender)
                .build();
    }

    @Override
    @Transactional
    public void verifyProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
        
        profile.setVerified(true);
        profileRepository.save(profile);

        // Notify user
        notificationService.createNotification(profile.getUser().getId(), "Your profile has been successfully verified by our administrative team!", NotificationType.GENERAL);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileDto> getPendingVerifications() {
        return profileRepository.findAll().stream()
                .filter(p -> !p.isVerified())
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    @Transactional
    public void resolveReport(Long reportId, String actionTaken) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);

        if ("SUSPEND".equalsIgnoreCase(actionTaken)) {
            User reportedUser = report.getReported();
            reportedUser.setEnabled(false); // suspend account
            userRepository.save(reportedUser);
            
            // Notify reporter
            notificationService.createNotification(report.getReporter().getId(), "Your report against user ID " + reportedUser.getId() + " has been resolved. The account has been suspended.", NotificationType.GENERAL);
        }
    }
}
