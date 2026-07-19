package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.InterestDto;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@PreAuthorize("isAuthenticated()")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/interests/send/{receiverId}")
    public ResponseEntity<ApiResponse<String>> sendInterest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long receiverId) {
        matchingService.sendInterest(userDetails.getId(), receiverId);
        return ResponseEntity.ok(ApiResponse.success("Interest request sent successfully"));
    }

    @PostMapping("/interests/accept/{interestId}")
    public ResponseEntity<ApiResponse<String>> acceptInterest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interestId) {
        matchingService.acceptInterest(userDetails.getId(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest request accepted. You are now matched!"));
    }

    @PostMapping("/interests/reject/{interestId}")
    public ResponseEntity<ApiResponse<String>> rejectInterest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interestId) {
        matchingService.rejectInterest(userDetails.getId(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest request rejected"));
    }

    @GetMapping("/interests/sent")
    public ResponseEntity<ApiResponse<List<InterestDto>>> getSentInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InterestDto> list = matchingService.getSentInterests(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Sent interest requests retrieved", list));
    }

    @GetMapping("/interests/received")
    public ResponseEntity<ApiResponse<List<InterestDto>>> getReceivedInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InterestDto> list = matchingService.getReceivedInterests(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Received interest requests retrieved", list));
    }

    @GetMapping("/mutual")
    public ResponseEntity<ApiResponse<List<ProfileDto>>> getMutualMatches(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProfileDto> list = matchingService.getMutualMatches(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Mutual matches retrieved successfully", list));
    }

    @PostMapping("/block/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId) {
        matchingService.blockUser(userDetails.getId(), targetUserId);
        return ResponseEntity.ok(ApiResponse.success("User blocked successfully"));
    }

    @PostMapping("/report/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> reportUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId,
            @RequestParam String reason,
            @RequestParam(required = false, defaultValue = "") String details) {
        matchingService.reportUser(userDetails.getId(), targetUserId, reason, details);
        return ResponseEntity.ok(ApiResponse.success("User reported successfully"));
    }

    @GetMapping("/compatibility/{targetUserId}")
    public ResponseEntity<ApiResponse<Double>> getCompatibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId) {
        double percentage = matchingService.calculateCompatibility(userDetails.getId(), targetUserId);
        return ResponseEntity.ok(ApiResponse.success("Compatibility score calculated", percentage));
    }
}
