package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.ProfileDto;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@PreAuthorize("isAuthenticated()")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProfileDto>>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String religion,
            @RequestParam(required = false) String caste,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double minHeight,
            @RequestParam(required = false) Double maxHeight,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ProfileDto> results = searchService.searchProfiles(
                userDetails.getId(), gender, religion, caste, minAge, maxAge, minHeight, maxHeight,
                education, occupation, minSalary, maxSalary, city, state, country, pageable);

        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", results));
    }
}
