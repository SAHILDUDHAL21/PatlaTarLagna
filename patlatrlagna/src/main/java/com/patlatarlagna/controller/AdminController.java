package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.DashboardStatsDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.entity.Report;
import com.patlatarlagna.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        DashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Admin stats retrieved successfully", stats));
    }

    @GetMapping("/verifications/pending")
    public ResponseEntity<ApiResponse<List<ProfileDto>>> getPendingVerifications() {
        List<ProfileDto> pending = adminService.getPendingVerifications();
        return ResponseEntity.ok(ApiResponse.success("Pending profile verifications retrieved", pending));
    }

    @PostMapping("/verifications/approve/{profileId}")
    public ResponseEntity<ApiResponse<String>> approveProfile(@PathVariable Long profileId) {
        adminService.verifyProfile(profileId);
        return ResponseEntity.ok(ApiResponse.success("Profile verified successfully"));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<Report>>> getReports() {
        List<Report> reports = adminService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success("All reported violations retrieved", reports));
    }

    @PostMapping("/reports/resolve/{reportId}")
    public ResponseEntity<ApiResponse<String>> resolveReport(
            @PathVariable Long reportId,
            @RequestParam(defaultValue = "RESOLVE") String action) {
        adminService.resolveReport(reportId, action);
        return ResponseEntity.ok(ApiResponse.success("Report violation resolved successfully"));
    }
}
