package com.patlatarlagna.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalUsers;
    private long pendingVerifications;
    private long totalReports;
    private Map<String, Long> userRegistrationTrend; // Year-Month vs count
    private Map<String, Long> distributionByGender;
}
