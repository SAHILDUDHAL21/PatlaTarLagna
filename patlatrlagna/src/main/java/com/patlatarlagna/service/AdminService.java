package com.patlatarlagna.service;

import com.patlatarlagna.dto.DashboardStatsDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.Report;
import java.util.List;

public interface AdminService {
    DashboardStatsDto getDashboardStats();
    void verifyProfile(Long profileId);
    List<ProfileDto> getPendingVerifications();
    List<Report> getAllReports();
    void resolveReport(Long reportId, String actionTaken);
}
