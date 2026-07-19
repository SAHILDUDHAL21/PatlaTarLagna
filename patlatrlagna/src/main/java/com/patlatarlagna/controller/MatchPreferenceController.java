package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.MatchPreferenceDto;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.service.MatchPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
@PreAuthorize("isAuthenticated()")
public class MatchPreferenceController {

    private final MatchPreferenceService preferenceService;

    public MatchPreferenceController(MatchPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<MatchPreferenceDto>> getMyPreference(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MatchPreferenceDto dto = preferenceService.getPreferenceByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Match preferences retrieved", dto));
    }

    @PutMapping("/my")
    public ResponseEntity<ApiResponse<MatchPreferenceDto>> updateMyPreference(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MatchPreferenceDto dto) {
        MatchPreferenceDto updated = preferenceService.updatePreference(userDetails.getId(), dto);
        return ResponseEntity.ok(ApiResponse.success("Match preferences updated successfully", updated));
    }
}
